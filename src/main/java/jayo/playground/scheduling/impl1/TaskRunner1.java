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
import jayo.playground.scheduling.TaskQueue;
import jayo.playground.scheduling.TaskRunner;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public final class TaskRunner1 implements TaskRunner {
    private final @NonNull ExecutorService executor;
    private final @NonNull Runnable runnable;

    final ReentrantLock lock = new ReentrantLock();
    final Condition condition = lock.newCondition();

    private final AtomicInteger nextQueueName = new AtomicInteger(10000);
    private boolean coordinatorWaiting = false;
    private long coordinatorWakeUpAt = 0L;

    private int executeCallCount = 0;
    private int runCallCount = 0;

    /**
     * Queues with tasks that are currently executing their {@link TaskQueue1#activeTask}.
     */
    private final List<TaskQueue1> busyQueues = new ArrayList<>();

    /**
     * Queues not in {@link #busyQueues} that have non-empty {@link TaskQueue1#futureTasks}.
     */
    private final List<TaskQueue1> readyQueues = new ArrayList<>();

    public TaskRunner1(final @NonNull ExecutorService executor) {
        assert executor != null;

        this.executor = executor;
        runnable = () -> {
            Task1 task;
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
                    final var delayNanos = task.runOnce();
                    // A task ran successfully. Update the execution state and take the next task.
                    lock.lock();
                    try {
                        afterRun(task, delayNanos, true);
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
                    afterRun(task, -1L, false);
                } finally {
                    lock.unlock();
                }
                throw thrown;
            } finally {
                currentThread.setName(oldName);
            }
        };
    }

    void kickCoordinator(final @NonNull TaskQueue1 taskQueue) {
        assert taskQueue != null;

        if (taskQueue.activeTask == null) {
            if (!taskQueue.futureTasks.isEmpty()) {
                addIfAbsent(readyQueues, taskQueue);
            } else {
                readyQueues.remove(taskQueue);
            }
        }

        if (coordinatorWaiting) {
            coordinatorNotify();
        } else {
            startAnotherThread();
        }
    }

    private void beforeRun(final @NonNull Task1 task) {
        assert task != null;

        task.nextExecuteNanoTime = -1L;
        final var queue = task.queue;
        assert queue != null;
        queue.futureTasks.remove(task);
        readyQueues.remove(queue);
        queue.activeTask = task;
        busyQueues.add(queue);
    }

    private void afterRun(final @NonNull Task1 task,
                          final long delayNanos,
                          final boolean completedNormally) {
        assert task != null;

        final var queue = task.queue;
        assert queue != null;
        if (queue.activeTask != task) {
            throw new IllegalStateException("Task queue " + queue.name + " is not active");
        }

        final var cancelActiveTask = queue.cancelActiveTask;
        queue.cancelActiveTask = false;
        queue.activeTask = null;
        busyQueues.remove(queue);

        if (delayNanos != -1L && !cancelActiveTask && !queue.shutdown) {
            queue.scheduleAndDecide(task, delayNanos);
        }

        if (!queue.futureTasks.isEmpty()) {
            readyQueues.add(queue);

            // If the task crashed, start another thread to run the next task.
            if (!completedNormally) {
                startAnotherThread();
            }
        }
    }

    /**
     * Returns an immediately-executable task for the calling thread to execute, sleeping as necessary
     * until one is ready. If there are no ready queues, or if other threads have everything under
     * control this will return null. If there is more than a single task ready to execute immediately
     * this will start another thread to handle that work.
     */
    private @Nullable Task1 awaitTaskToRun() {
        while (true) {
            if (readyQueues.isEmpty()) {
                return null; // Nothing to do.
            }

            final var now = nanoTime();
            var minDelayNanos = Long.MAX_VALUE;
            Task1 readyTask = null;
            var multipleReadyTasks = false;

            // 1) Decide what to run. This loop's goal wants to:
            //  * Find out what this thread should do (either run a task or sleep)
            //  * Find out if there's enough work to start another thread.
            for (final var queue : readyQueues) {
                final var candidate = queue.futureTasks.first();
                final var candidateDelay = Math.max(0L, candidate.nextExecuteNanoTime - now);

                // Compute the delay of the soonest-executable task.
                if (candidateDelay > 0L) {
                    minDelayNanos = Math.min(candidateDelay, minDelayNanos);

                    // If we already have more than one task, that's enough work for now. Stop searching.
                } else if (readyTask != null) {
                    multipleReadyTasks = true;
                    break;

                    // We have a task to execute when we complete the loop.
                } else {
                    readyTask = candidate;
                }
            }

            // Implement the decision.
            // We have a task ready to go. Get ready.
            if (readyTask != null) {
                beforeRun(readyTask);

                // Also start another thread if there's more work or scheduling to do.
                if (multipleReadyTasks || (!coordinatorWaiting && !readyQueues.isEmpty())) {
                    startAnotherThread();
                }

                return readyTask;

                // Notify the coordinator of a task that's coming up soon.
            } else if (coordinatorWaiting) {
                if (minDelayNanos < coordinatorWakeUpAt - now) {
                    coordinatorNotify();
                }
                return null;

                // No other thread is coordinating. Become the coordinator!
            } else {
                coordinatorWaiting = true;
                coordinatorWakeUpAt = now + minDelayNanos;
                try {
                    coordinatorWait(minDelayNanos);
                } catch (InterruptedException ignored) {
                    // Will cause all tasks to exit unless more are scheduled!
                    cancelAll();
                } finally {
                    coordinatorWaiting = false;
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
        return newScheduledQueue();
    }

    @Override
    public @NonNull ScheduledTaskQueue newScheduledQueue() {
        return new TaskQueue1(this, "Q" + nextQueueName.getAndIncrement());
    }

    @Override
    public void execute(boolean cancellable, Runnable block) {
        final var queue = newQueue();
        queue.execute(queue.getName() + "-task", cancellable, block);
        try {
            queue.idleLatch().await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            queue.shutdown();
        }
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    public long nanoTime() {
        return System.nanoTime();
    }

    public void coordinatorNotify() {
        condition.signal();
    }

    /**
     * Wait a duration in nanoseconds. Unlike {@link Object#wait(long)} this interprets 0 as "don't wait" instead of
     * "wait forever".
     */
    public void coordinatorWait(long nanos) throws InterruptedException {
        if (nanos > 0) {
            condition.awaitNanos(nanos);
        }
    }

    public void execute(@NonNull Runnable runnable) {
        executor.execute(runnable);
    }

    private void cancelAll() {
        for (var i = busyQueues.size() - 1; i >= 0; i--) {
            busyQueues.get(i).cancelAllAndDecide();
        }
        for (var i = readyQueues.size() - 1; i >= 0; i--) {
            final var queue = readyQueues.get(i);
            queue.cancelAllAndDecide();
            if (queue.futureTasks.isEmpty()) {
                readyQueues.remove(i);
            }
        }
    }

    static <T> void addIfAbsent(final Collection<T> collection, final T element) {
        if (!collection.contains(element)) {
            collection.add(element);
        }
    }
}
