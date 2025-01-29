/*
 * Copyright (c) 2025-present, pull-vert and Jayo contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 *
 * Forked from OkHttp (https://github.com/square/okhttp), original copyright is below
 *
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jayo.playground.scheduling.impl1;

import jayo.playground.scheduling.ScheduledTaskQueue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.LongSupplier;

final class TaskQueue1 implements ScheduledTaskQueue {
    final @NonNull TaskRunner1 taskRunner;
    final @NonNull String name;

    boolean shutdown = false;

    /**
     * Scheduled tasks ordered by {@link Task1#nextExecuteNanoTime}.
     */
    @NonNull
    final NavigableSet<Task1> futureTasks = new TreeSet<>();

    /**
     * This queue's currently-executing task, or null if none is currently executing.
     */
    @Nullable
    Task1 activeTask = null;

    /**
     * True if the {@link #activeTask} should be canceled when it completes.
     */
    boolean cancelActiveTask = false;

    TaskQueue1(final @NonNull TaskRunner1 taskRunner, final @NonNull String name) {
        assert taskRunner != null;
        assert name != null;

        this.taskRunner = taskRunner;
        this.name = name;
    }

    @Override
    public @NonNull String getName() {
        return name;
    }

    @Override
    public void schedule(final @NonNull String name, final long initialDelayNanos, final @NonNull LongSupplier block) {
        assert name != null;
        assert initialDelayNanos >= 0;
        assert block != null;

        schedule(new Task1(name, true) {
                     @Override
                     protected long runOnce() {
                         return block.getAsLong();
                     }
                 },
                initialDelayNanos);
    }

    @Override
    public void execute(final @NonNull String name, final boolean cancellable, final @NonNull Runnable block) {
        assert name != null;
        assert block != null;

        schedule(new Task1(name, cancellable) {
                     @Override
                     protected long runOnce() {
                         block.run();
                         return -1L;
                     }
                 },
                0L);
    }

    /**
     * Schedules {@code task} for execution in {@code delayNanos}. A task may only have one future execution scheduled.
     * If the task is already in the queue, the earliest execution time is used.
     * <p>
     * The target execution time is implemented on a best-effort basis. If another task in this queue is running when
     * that time is reached, that task is allowed to complete before this task is started. Similarly, the task will be
     * delayed if the host lacks compute resources.
     *
     * @throws RejectedExecutionException if the queue is shut down and the task is not cancelable.
     */
    private void schedule(final @NonNull Task1 task, final long delayNanos) {
        assert task != null;
        assert delayNanos >= 0;

        taskRunner.lock.lock();
        try {
            if (shutdown) {
                if (task.cancellable) {
                    return;
                }
                throw new RejectedExecutionException("Queue is shutdown");
            }

            if (scheduleAndDecide(task, delayNanos)) {
                taskRunner.kickCoordinator(this);
            }
        } finally {
            taskRunner.lock.unlock();
        }
    }

    @Override
    public @NonNull CountDownLatch idleLatch() {
        taskRunner.lock.lock();
        try {
            // If the queue is already idle, that's easy.
            if (activeTask == null && futureTasks.isEmpty()) {
                return new CountDownLatch(0);
            }

            // If there's an existing AwaitIdleTask, use it. This is necessary when the executor is shutdown but still
            // busy as we can't enqueue in that case.
            if (activeTask instanceof AwaitIdleTask existingAwaitIdleTask) {
                return existingAwaitIdleTask.latch;
            }
            for (final var futureTask : futureTasks) {
                if (futureTask instanceof AwaitIdleTask futureAwaitIdleTask) {
                    return futureAwaitIdleTask.latch;
                }
            }

            // Don't delegate to schedule() because that enforces shutdown rules.
            final var newTask = new AwaitIdleTask();
            if (scheduleAndDecide(newTask, 0L)) {
                taskRunner.kickCoordinator(this);
            }
            return newTask.latch;
        } finally {
            taskRunner.lock.unlock();
        }
    }

    private static final class AwaitIdleTask extends Task1 {
        private final @NonNull CountDownLatch latch = new CountDownLatch(1);

        private AwaitIdleTask() {
            super("Jayo awaitIdle", false);
        }

        @Override
        protected long runOnce() {
            latch.countDown();
            return -1L;
        }
    }

    boolean scheduleAndDecide(final @NonNull Task1 task, final long delayNanos) {
        assert task != null;

        task.initQueue(this);

        final var now = taskRunner.nanoTime();
        final var executeNanoTime = now + delayNanos;

        // If the task is already scheduled, take the earlier of the two times.
        if (futureTasks.contains(task)) {
            if (task.nextExecuteNanoTime <= executeNanoTime) {
                return false;
            }
            futureTasks.remove(task); // Already scheduled later: reschedule below!
        }
        task.nextExecuteNanoTime = executeNanoTime;

        // Insert in chronological order.
        futureTasks.add(task);

        // Impact the coordinator if we inserted at the front.
        return futureTasks.first() == task;
    }

    @Override
    public void shutdown() {
        taskRunner.lock.lock();
        try {
            shutdown = true;
            if (cancelAllAndDecide()) {
                taskRunner.kickCoordinator(this);
            }
        } finally {
            taskRunner.lock.unlock();
        }
    }

    /**
     * @return true if the coordinator is impacted.
     */
    boolean cancelAllAndDecide() {
        if (activeTask != null && activeTask.cancellable) {
            cancelActiveTask = true;
        }

        var tasksCanceled = false;
        final var descendingTasksIterator = futureTasks.descendingIterator();
        while (descendingTasksIterator.hasNext()) {
            final var task = descendingTasksIterator.next();
            if (task.cancellable) {
                tasksCanceled = true;
                descendingTasksIterator.remove();
            }
        }

        return tasksCanceled;
    }

    @Override
    public @NonNull String toString() {
        return name;
    }
}
