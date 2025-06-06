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

package jayo.playground.scheduling.impl3;

import jayo.playground.scheduling.ScheduledTaskQueue;
import jayo.playground.scheduling.TaskQueue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.LongSupplier;

sealed abstract class TaskQueue3<T extends Task3<T>> implements TaskQueue {
    final @NonNull TaskRunner3 taskRunner;
    final @NonNull String name;

    boolean shutdown = false;

    @NonNull
    final Queue<T> futureTasks;

    /**
     * This queue's currently waiting for execution task in the {@link TaskRunner3}, or null if no future tasks.
     */
    @Nullable
    T scheduledTask = null;

    /**
     * This queue's currently-executing task, or null if none is currently executing.
     */
    @Nullable
    T activeTask = null;

    /**
     * True if the {@link #activeTask} should be canceled when it completes.
     */
    boolean cancelActiveTask = false;

    TaskQueue3(final @NonNull TaskRunner3 taskRunner,
               final @NonNull String name,
               final @NonNull Queue<T> futureTasks) {
        assert taskRunner != null;
        assert name != null;
        assert futureTasks != null;

        this.taskRunner = taskRunner;
        this.name = name;
        this.futureTasks = futureTasks;
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
    void schedule(final @NonNull T task, final long delayNanos) {
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

    abstract boolean scheduleAndDecide(final @NonNull T task, final long delayNanos);

    @Override
    public @NonNull String getName() {
        return name;
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
                // also remove from the task runner
                if (scheduledTask == task) {
                    tasksCanceled = true;
                    removeFromTaskRunner(task);
                }
            }
        }
        return tasksCanceled;
    }

    abstract void removeFromTaskRunner(final @NonNull T task);

    @Override
    public @NonNull String toString() {
        return name;
    }

    static final class ScheduledQueue extends TaskQueue3<Task3.ScheduledTask> implements ScheduledTaskQueue {
        ScheduledQueue(final @NonNull TaskRunner3 taskRunner, final @NonNull String name) {
            super(taskRunner, name, new PriorityQueue<>());
        }

        @Override
        public void schedule(final @NonNull String name, final long initialDelayNanos, final @NonNull LongSupplier block) {
            assert name != null;
            assert initialDelayNanos >= 0;
            assert block != null;

            schedule(new Task3.@NonNull ScheduledTask(name, true) {
                @Override
                protected long runOnce() {
                    return block.getAsLong();
                }
            }, initialDelayNanos);
        }

        @Override
        public void execute(final @NonNull String name, final boolean cancellable, final @NonNull Runnable block) {
            assert name != null;
            assert block != null;

            schedule(new Task3.@NonNull ScheduledTask(name, cancellable) {
                @Override
                protected long runOnce() {
                    block.run();
                    return -1L;
                }
            }, 0L);
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

        static final class AwaitIdleTask extends Task3.ScheduledTask {
            private final @NonNull CountDownLatch latch = new CountDownLatch(1);

            private AwaitIdleTask() {
                super("Jayo awaitIdle", false);
            }

            @Override
            long runOnce() {
                latch.countDown();
                return -1L;
            }
        }

        @Override
        boolean scheduleAndDecide(final Task3.@NonNull ScheduledTask task, final long delayNanos) {
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
            futureTasks.offer(task);

            if (scheduledTask != null) {
                // a task was already in the task runner, take the earlier of the two times.
                if (scheduledTask.nextExecuteNanoTime <= executeNanoTime) {
                    return false;
                }
                taskRunner.futureScheduledTasks.remove(scheduledTask);
            }

            scheduledTask = task;
            taskRunner.futureScheduledTasks.offer(task);

            // Impact the coordinator if we inserted at the front.
            return taskRunner.futureScheduledTasks.element() == task;
        }

        @Override
        void removeFromTaskRunner(final Task3.@NonNull ScheduledTask task) {
            assert task != null;
            taskRunner.futureScheduledTasks.remove(task);
        }
    }

    static final class RunnableQueue extends TaskQueue3<Task3.RunnableTask> {
        RunnableQueue(final @NonNull TaskRunner3 taskRunner, final @NonNull String name) {
            super(taskRunner, name, new LinkedList<>());
        }

        @Override
        public void execute(final @NonNull String name, final boolean cancellable, final @NonNull Runnable block) {
            assert name != null;
            assert block != null;

            schedule(new Task3.@NonNull RunnableTask(name, cancellable) {
                @Override
                public void run() {
                    block.run();
                }
            }, 0L);
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

        static final class AwaitIdleTask extends Task3.RunnableTask {
            private final @NonNull CountDownLatch latch = new CountDownLatch(1);

            private AwaitIdleTask() {
                super("Jayo awaitIdle", false);
            }

            @Override
            public void run() {
                latch.countDown();
            }
        }

        @Override
        boolean scheduleAndDecide(final Task3.@NonNull RunnableTask task, long delayNanos) {
            assert task != null;

            task.initQueue(this);

            // If the task is already scheduled, do nothing.
            if (futureTasks.contains(task)) {
                return false;
            }

            // Insert in FIFO order.
            futureTasks.offer(task);

            if (scheduledTask != null) {
                return false;
            }

            scheduledTask = task;
            taskRunner.futureTasks.offer(task);
            return true;
        }

        @Override
        void removeFromTaskRunner(final Task3.@NonNull RunnableTask task) {
            assert task != null;
            taskRunner.futureTasks.remove(task);
        }
    }
}
