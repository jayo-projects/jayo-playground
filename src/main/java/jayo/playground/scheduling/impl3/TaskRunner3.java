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
import jayo.playground.scheduling.TaskRunner;
import org.jspecify.annotations.NonNull;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public final class TaskRunner3 implements TaskRunner {
    private final @NonNull ExecutorService executor;
    private final @NonNull Runnable runnable;

    final ReentrantLock lock = new ReentrantLock();
    final Condition condition = lock.newCondition();

    private final AtomicLong nextTaskIndex = new AtomicLong(10000);
    private final AtomicInteger nextQueueIndex = new AtomicInteger(10000);
    private boolean coordinatorWaiting = false;
    private long coordinatorWakeUpAt = 0L;

    private int executeCallCount = 0;
    private int runCallCount = 0;

    /**
     * sequential tasks FIFO ordered.
     */
    final Queue<Task3.RunnableTask> futureTasks = new LinkedList<>();
    /**
     * Scheduled tasks ordered by {@link Task3.ScheduledTask#nextExecuteNanoTime}.
     */
    final Queue<Task3.ScheduledTask> futureScheduledTasks = new PriorityQueue<>();

    @SuppressWarnings({"unchecked", "RawUseOfParameterized"})
    public TaskRunner3(final @NonNull ExecutorService executor) {
        assert executor != null;

        this.executor = executor;
        runnable = () -> {
            Task3<?> task;
            lock.lock();
            try {
                runCallCount++;
                task = awaitTaskToRun();
                if (task == null) {
                    return;
                }
            } finally {
                lock.unlock();
            }

            final var currentThread = Thread.currentThread();
            final var oldName = currentThread.getName();
            try {
                while (true) {
                    currentThread.setName(task.name);
                    final var delayNanos = switch (task) {
                        case Task3.RunnableTask runnableTask -> {
                            runnableTask.run();
                            yield -1L;
                        }
                        case Task3.ScheduledTask scheduledTask -> scheduledTask.runOnce();
                        default -> throw new IllegalStateException("Unexpected task type: " + task);
                    };
                    // A task ran successfully. Update the execution state and take the next task.
                    lock.lock();
                    try {
                        afterRun((Task3) task, delayNanos, true);
                        task = awaitTaskToRun();
                        if (task == null) {
                            return;
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            } catch (Throwable thrown) {
                // A task failed. Update execution state and re-throw the exception.
                lock.lock();
                try {
                    assert task != null;
                    afterRun((Task3) task, -1L, false);
                } finally {
                    lock.unlock();
                }
                throw thrown;
            } finally {
                currentThread.setName(oldName);
            }
        };
    }

    void kickCoordinator() {
        if (coordinatorWaiting) {
            coordinatorNotify();
        } else {
            startAnotherThread();
        }
    }

    private <T extends Task3<T>> void beforeRun(final @NonNull T task) {
        assert task != null;

        final var queue = task.queue;
        if (queue != null) {
            final var removedTask = queue.futureTasks.remove();
            if (removedTask != task || queue.scheduledTask != task) {
                throw new IllegalStateException("removedTask " + removedTask + " or queue.scheduledTask " +
                        queue.scheduledTask + " != task " + task);
            }
            queue.activeTask = task;
        }

        switch (task) {
            case Task3.RunnableTask runnableTask -> {
                if (futureTasks.remove() != runnableTask) {
                    throw new IllegalStateException();
                }
            }
            case Task3.ScheduledTask scheduledTask -> {
                scheduledTask.nextExecuteNanoTime = -1L;
                if (futureScheduledTasks.remove() != scheduledTask) {
                    throw new IllegalStateException();
                }
            }
            default -> throw new IllegalStateException("Unexpected task type: " + task);
        }

        // Also start another thread if there's more work or scheduling to do.
        if (!futureTasks.isEmpty() || !futureScheduledTasks.isEmpty()) {
            startAnotherThread();
        }
    }

    private <T extends Task3<T>> void afterRun(final @NonNull T task,
                          final long delayNanos,
                          final boolean completedNormally) {
        assert task != null;

        final var queue = task.queue;
        if (queue != null) {
            afterRun(task, delayNanos, queue);
        }

        // If the task crashed, start another thread to run the next task.
        if ((!futureTasks.isEmpty() || !futureScheduledTasks.isEmpty()) && !completedNormally) {
            startAnotherThread();
        }
    }

    private <T extends Task3<T>> void afterRun(final @NonNull T task,
                                               final long delayNanos,
                                               final @NonNull TaskQueue3<T> queue) {
        if (queue.activeTask != task) {
            throw new IllegalStateException("Task queue " + queue.name + " is not active." +
                    " queue.activeTask " + queue.activeTask + " != task " + task);
        }

        final var cancelTask = queue.cancelActiveTask;
        queue.cancelActiveTask = false;
        queue.activeTask = null;

        assert queue.scheduledTask == task;

        final var nextTaskInQueue = queue.futureTasks.peek();
        if (nextTaskInQueue != null) {
            switch (nextTaskInQueue) {
                case Task3.RunnableTask runnableTask -> futureTasks.offer(runnableTask);
                case Task3.ScheduledTask scheduledTask -> futureScheduledTasks.offer(scheduledTask);
                default -> throw new IllegalStateException("Unexpected task type: " + task);
            }
            queue.scheduledTask = nextTaskInQueue;
        } else {
            queue.scheduledTask = null;
        }

        if (delayNanos != -1L && !cancelTask && !queue.shutdown) {
            queue.scheduleAndDecide(task, delayNanos);
        }
    }

    /**
     * Returns an immediately-executable task for the calling thread to execute, sleeping as necessary until one is
     * ready. If there are no ready task, or if other threads can execute it this will return null. If there is more
     * than a single task ready to execute immediately this will start another thread to handle that work.
     */
    private Task3<?> awaitTaskToRun() {
        while (true) {
            // 1) try to peek runnable tasks
            final var task = futureTasks.peek();
            // We have a task ready to go. Run it.
            if (task != null) {
                beforeRun(task);
                return task;
            }

            // 2) try to peek scheduled tasks
            final var scheduledTask = futureScheduledTasks.peek();
            if (scheduledTask == null) {
                return null; // Nothing to do.
            }

            final var now = nanoTime();
            final var taskDelayNanos = scheduledTask.nextExecuteNanoTime - now;

            // We have a task ready to go. Run it.
            if (taskDelayNanos <= 0L) {
                beforeRun(scheduledTask);
                return scheduledTask;

                // Notify the coordinator of a task that's coming up soon.
            } else if (coordinatorWaiting) {
                if (taskDelayNanos < coordinatorWakeUpAt - now) {
                    coordinatorNotify();
                }
                return null;

                // No other thread is coordinating. Become the coordinator and wait for this scheduled task!
            } else {
                coordinatorWaiting = true;
                coordinatorWakeUpAt = now + taskDelayNanos;
                var fullyWaited = false;
                try {
                    fullyWaited = coordinatorWait(taskDelayNanos);
                } catch (InterruptedException ignored) {
                    // Will cause all tasks to exit unless more are scheduled!
                    cancelAll();
                } finally {
                    coordinatorWaiting = false;
                }
                // wait was fully done, return this scheduled task now ready to go.
                if (fullyWaited && scheduledTask == futureScheduledTasks.peek()) {
                    beforeRun(scheduledTask);
                    return scheduledTask;
                }
            }
        }
    }

    /**
     * Start another thread, unless a new thread is already scheduled to start.
     */
    private void startAnotherThread() {
        if (executeCallCount > runCallCount) {
            return; // A thread is still starting.
        }
        executeCallCount++;
        execute(runnable);
    }

    @Override
    public @NonNull TaskQueue newQueue() {
        return new TaskQueue3.RunnableQueue(this, "Q" + nextQueueIndex.getAndIncrement());
    }

    @Override
    public @NonNull ScheduledTaskQueue newScheduledQueue() {
        return new TaskQueue3.ScheduledQueue(this, "Q" + nextQueueIndex.getAndIncrement());
    }

    @Override
    public void execute(final boolean cancellable, final Runnable block) {
        assert block != null;
        final var task = new Task3.RunnableTask("T" + nextTaskIndex, cancellable) {
            @Override
            public void run() {
                block.run();
            }
        };

        lock.lock();
        try {
            futureTasks.offer(task);
            kickCoordinator();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void shutdown() {
        lock.lock();
        try {
            cancelAll();
            executor.shutdown();
        } finally {
            lock.unlock();
        }
    }

    public long nanoTime() {
        return System.nanoTime();
    }

    public void coordinatorNotify() {
        condition.signal();
    }

    /**
     * Wait a duration in nanoseconds.
     *
     * @return true if wait was fully completed, false if it has been signalled before ending the wait phase.
     */
    public boolean coordinatorWait(final long nanos) throws InterruptedException {
        assert nanos > 0;
        return condition.awaitNanos(nanos) <= 0;
    }

    public void execute(final @NonNull Runnable runnable) {
        executor.execute(runnable);
    }

    private void cancelAll() {
        cancelAll(futureTasks);
        cancelAll(futureScheduledTasks);
    }

    private <T extends Task3<T>> void cancelAll(final @NonNull Queue<T> futureTasks) {
        assert futureTasks != null;

        final var tasksIterator = futureTasks.iterator();
        while (tasksIterator.hasNext()) {
            final var task = tasksIterator.next();
            if (task.cancellable) {
                tasksIterator.remove();
                if (task.queue != null) {
                    task.queue.futureTasks.remove(task);
                }
            }
        }
    }
}
