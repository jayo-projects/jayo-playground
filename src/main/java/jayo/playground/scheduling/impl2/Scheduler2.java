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

import jayo.playground.scheduling.Scheduler;
import jayo.playground.scheduling.TaskQueue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public final class Scheduler2 implements Scheduler {
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
     * Scheduled tasks ordered by {@link Task2#nextExecuteNanoTime}.
     */
    final Queue<Task2> futureTasks = new PriorityQueue<>();

    public Scheduler2(final @NonNull ExecutorService executor) {
        assert executor != null;

        this.executor = executor;
        runnable = () -> {
            Task2 task;
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

    void kickCoordinator() {
        if (coordinatorWaiting) {
            coordinatorNotify();
        } else {
            startAnotherThread();
        }
    }

    private void beforeRun(final @NonNull Task2 task) {
        assert task != null;

        task.nextExecuteNanoTime = -1L;
        final var queue = task.queue;
        assert queue != null;
        if (queue.futureTasks.remove() != task || queue.scheduledTask != task) {
            throw new IllegalStateException();
        }
        queue.activeTask = task;
        if (futureTasks.remove() != task) {
            throw new IllegalStateException();
        }

        // Also start another thread if there's more work or scheduling to do.
        if (!futureTasks.isEmpty()) {
            startAnotherThread();
        }
    }

    private void afterRun(final @NonNull Task2 task,
                          final long delayNanos,
                          final boolean completedNormally) {
        assert task != null;

        final var queue = task.queue;
        assert queue != null;
        if (queue.activeTask != task) {
            throw new IllegalStateException("Task queue " + queue.name + " is not active");
        }

        final var cancelTask = queue.cancelActiveTask;
        queue.cancelActiveTask = false;
        queue.activeTask = null;

        if (queue.scheduledTask != task) {
            throw new IllegalStateException();
        }
        final var nextTaskInQueue = queue.futureTasks.peek();
        if (nextTaskInQueue != null) {
            futureTasks.add(nextTaskInQueue);
            queue.scheduledTask = nextTaskInQueue;
        } else {
            queue.scheduledTask = null;
        }

        if (delayNanos != -1L && !cancelTask && !queue.shutdown) {
            queue.scheduleAndDecide(task, delayNanos);
        }

        // If the task crashed, start another thread to run the next task.
        if (!futureTasks.isEmpty() && !completedNormally) {
            startAnotherThread();
        }
    }

    /**
     * Returns an immediately-executable task for the calling thread to execute, sleeping as necessary until one is
     * ready. If there are no ready task, or if other threads can execute it this will return null. If there is more
     * than a single task ready to execute immediately this will start another thread to handle that work.
     */
    private @Nullable Task2 awaitTaskToRun() {
        while (true) {
            final var task = futureTasks.peek();
            if (task == null) {
                return null; // Nothing to do.
            }

            final var now = nanoTime();
            final var taskDelayNanos = task.nextExecuteNanoTime - now;

            // We have a task ready to go. Run it.
            if (taskDelayNanos <= 0L) {
                beforeRun(task);
                return task;

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
                if (fullyWaited && task == futureTasks.peek()) {
                    beforeRun(task);
                    return task;
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
        return new TaskQueue2(this, "Q" + nextQueueName.getAndIncrement(), new PriorityQueue<>());
    }

    @Override
    public void execute(final boolean cancellable, final Runnable block) {
        assert block != null;

        // todo build the task and call
        //new TaskQueue3(this, "Q" + nextQueueName.getAndIncrement(), Collections.singleton(task));
        final var queue = newQueue();
        queue.execute(queue.getName() + "-task", cancellable, block);
        try {
            queue.idleLatch().await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            queue.shutdownNow();
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
        final var tasksIterator = futureTasks.iterator();
        while (tasksIterator.hasNext()) {
            final var task = tasksIterator.next();
            if (task.cancellable) {
                tasksIterator.remove();
                assert task.queue != null;
                task.queue.futureTasks.remove(task);
            }
        }
    }
}
