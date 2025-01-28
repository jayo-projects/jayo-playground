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

package jayo.playground.scheduling.impl2;

import jayo.playground.scheduling.ScheduledTaskQueue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.LongSupplier;

final class TaskQueue2 implements ScheduledTaskQueue {
    final @NonNull TaskRunner2 taskRunner;
    final @NonNull String name;

    boolean shutdown = false;

    /**
     * Unordered scheduled tasks.
     */
    @NonNull
    final Queue<Task2> futureTasks;

    /**
     * This queue's currently-executing task, or null if none is currently executing.
     */
    @Nullable
    Task2 scheduledTask = null;

    /**
     * This queue's currently-executing task, or null if none is currently executing.
     */
    @Nullable
    Task2 activeTask = null;

    /**
     * True if the {@link #activeTask} should be canceled when it completes.
     */
    boolean cancelActiveTask = false;

    TaskQueue2(final @NonNull TaskRunner2 taskRunner,
               final @NonNull String name,
               final @NonNull Queue<Task2> futureTasks) {
        assert taskRunner != null;
        assert name != null;
        assert futureTasks != null;

        this.taskRunner = taskRunner;
        this.name = name;
        this.futureTasks = futureTasks;
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

        schedule(new Task2(name, true) {
                     @Override
                     protected long runOnce() {
                         return block.getAsLong();
                     }
                 },
                initialDelayNanos);
    }

    @Override
    public void execute(final @NonNull String name, final boolean cancellable, final Runnable block) {
        assert name != null;
        assert block != null;

        schedule(new Task2(name, cancellable) {
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
    private void schedule(final @NonNull Task2 task, final long delayNanos) {
        assert task != null;
        assert delayNanos >= 0;

        taskRunner.lock.lock();
        try {
            if (shutdown) {
                if (task.cancellable) {
                    return;
                }
                throw new RejectedExecutionException();
            }

            if (scheduleAndDecide(task, delayNanos)) {
                taskRunner.kickCoordinator();
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
            if (futureTasks.isEmpty()) {
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
                taskRunner.kickCoordinator();
            }
            return newTask.latch;
        } finally {
            taskRunner.lock.unlock();
        }
    }

    private static final class AwaitIdleTask extends Task2 {
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

    boolean scheduleAndDecide(final @NonNull Task2 task, final long delayNanos) {
        assert task != null;

        task.initQueue(this);

        final var executeNanoTime = taskRunner.nanoTime() + delayNanos;

        // If the task is already scheduled, take the earlier of the two times.
        if (futureTasks.contains(task)) {
            if (task.nextExecuteNanoTime <= executeNanoTime) {
                return false;
            }
            // Already scheduled later: reschedule below!
            futureTasks.remove(task);
        }
        task.nextExecuteNanoTime = executeNanoTime;

        // Insert in chronological order.
        futureTasks.add(task);

        if (scheduledTask != null) {
            // a task was already in the scheduler, take the earlier of the two times.
            if (scheduledTask.nextExecuteNanoTime <= executeNanoTime) {
                return false;
            }
            taskRunner.futureTasks.remove(scheduledTask);
        }

        scheduledTask = task;
        taskRunner.futureTasks.add(task);

        // Impact the coordinator if we inserted at the front.
        return taskRunner.futureTasks.element() == task;
    }

    @Override
    public void shutdown() {
        taskRunner.lock.lock();
        try {
            shutdown = true;
            if (cancelAllAndDecide()) {
                taskRunner.kickCoordinator();
            }
        } finally {
            taskRunner.lock.unlock();
        }
    }

    /**
     * @return true if the coordinator is impacted.
     */
    @SuppressWarnings("UnusedReturnValue")
    boolean cancelAllAndDecide() {
        if (activeTask != null && activeTask.cancellable) {
            cancelActiveTask = true;
        }

        var tasksCanceled = false;
        final var tasksIterator = futureTasks.iterator();
        while (tasksIterator.hasNext()) {
            final var task = tasksIterator.next();
            if (task.cancellable) {
                tasksIterator.remove();
                // also remove from the scheduler
                if (scheduledTask == task) {
                    tasksCanceled = true;
                    taskRunner.futureTasks.remove(task);
                }
            }
        }

        return tasksCanceled;
    }

    @Override
    public @NonNull String toString() {
        return name;
    }
}
