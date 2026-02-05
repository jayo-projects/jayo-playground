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

package jayo.playground.scheduling.impl6;

import jayo.playground.scheduling.BasicFifoQueue;
import jayo.playground.scheduling.ScheduledTaskQueue;
import jayo.playground.scheduling.TaskQueue;
import jayo.playground.scheduling.TaskRunner;
import org.jspecify.annotations.NonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class TaskRunner6 implements TaskRunner {
    private final @NonNull Executor executor;

    private final @NonNull AtomicInteger nextQueueIndex = new AtomicInteger(10000);

    // scheduled runner
    private final @NonNull Runnable scheduledRunnable;
    final @NonNull Lock scheduledLock = new ReentrantLock();
    final @NonNull Condition scheduledCondition = scheduledLock.newCondition();
    private boolean scheduledCoordinatorWaiting = false;
    private long scheduledCoordinatorWakeUpAt = 0L;
    private int scheduledExecuteCallCount = 0;
    private int scheduledRunCallCount = 0;

    // FIFO runner
    private final @NonNull Runnable runnable;
    final @NonNull Lock lock = new ReentrantLock();
    private int executeCallCount = 0;
    private int runCallCount = 0;

    // termination
    private final @NonNull Set<@NonNull Thread> threads = ConcurrentHashMap.newKeySet();
    private final @NonNull CountDownLatch terminationSignal = new CountDownLatch(1);

    // state lifecycle: RUNNING -> SHUTDOWN_STARTED -> SHUTDOWN
    private static final int RUNNING = 0;
    private static final int SHUTDOWN_STARTED = 1;
    private static final int SHUTDOWN = 2;
    private volatile int state;
    private static final @NonNull VarHandle STATE;

    static {
        try {
            final var l = MethodHandles.lookup();
            STATE = l.findVarHandle(TaskRunner6.class, "state", int.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * sequential tasks FIFO ordered.
     */
    final BasicFifoQueue<Task6.RunnableTask> futureTasks = BasicFifoQueue.create();
    /**
     * Scheduled tasks ordered by {@link Task6.ScheduledTask#nextExecuteNanoTime}.
     */
    final Queue<Task6.ScheduledTask> futureScheduledTasks = new PriorityQueue<>();

    public TaskRunner6(final @NonNull Executor executor) {
        assert executor != null;

        this.executor = executor;
        scheduledRunnable = () -> {
            Task6.ScheduledTask task;
            scheduledLock.lock();
            try {
                scheduledRunCallCount++;
                task = awaitScheduledTaskToRun();
                if (task == null) {
                    return;
                }
            } finally {
                scheduledLock.unlock();
            }

            final var currentThread = Thread.currentThread();
            threads.add(currentThread);

            final var oldName = currentThread.getName();
            try {
                while (!Thread.interrupted()) {
                    assert task.name != null;
                    currentThread.setName(task.name);
                    final var delayNanos = task.runOnce();
                    // A task ran successfully. Update the execution state and take the next task.
                    scheduledLock.lock();
                    try {
                        afterScheduledRun(task, delayNanos, true);
                        task = awaitScheduledTaskToRun();
                        if (task == null) {
                            return;
                        }
                    } finally {
                        scheduledLock.unlock();
                    }
                }
            } catch (Throwable thrown) {
                // A task failed. Update execution state and re-throw the exception.
                scheduledLock.lock();
                try {
                    assert task != null;
                    afterScheduledRun(task, -1L, false);
                } finally {
                    scheduledLock.unlock();
                }
                throw thrown;
            } finally {
                executionComplete(currentThread);
                currentThread.setName(oldName);
            }
        };

        runnable = () -> {
            Task6.RunnableTask task;
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
            threads.add(currentThread);

            final var oldName = currentThread.getName();
            var threadNameChanged = false;
            try {
                while (!Thread.interrupted()) {
                    if (task.name != null) {
                        currentThread.setName(task.name);
                        threadNameChanged = true;
                    }
                    task.run();
                    // A task ran successfully. Update the execution state and take the next task.
                    lock.lock();
                    try {
                        afterRun(task, true);
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
                    afterRun(task, false);
                } finally {
                    lock.unlock();
                }
                if (!(thrown instanceof InterruptedException)) {
                    throw thrown;
                }
            } finally {
                executionComplete(currentThread);
                if (threadNameChanged) {
                    currentThread.setName(oldName);
                }
            }
        };
    }

    private boolean isShuttingDown() {
        return state >= SHUTDOWN_STARTED;
    }

    void ensureRunning() {
        if (isShuttingDown()) {
            // shutdown or terminated
            throw new RejectedExecutionException();
        }
    }

    private void executionComplete(final @NonNull Thread thread) {
        assert thread != null;

        boolean removed = threads.remove(thread);
        assert removed;
        if (state == SHUTDOWN_STARTED) {
            tryShutdown();
        }
    }

    /**
     * Try to terminate if already shutdown.
     */
    private void tryShutdown() {
        if (threads.isEmpty()
                && STATE.compareAndSet(this, SHUTDOWN_STARTED, SHUTDOWN)) {
            // signaling termination is done
            terminationSignal.countDown();
        }
    }

    void kickScheduledCoordinator() {
        if (scheduledCoordinatorWaiting) {
            coordinatorNotify();
        } else {
            startAnotherScheduledThread();
        }
    }

    /**
     * Start another thread, unless a new thread is already scheduled to start.
     */
    private void startAnotherScheduledThread() {
        if (scheduledExecuteCallCount > scheduledRunCallCount) {
            return; // A thread is still starting.
        }
        scheduledExecuteCallCount++;
        execute(scheduledRunnable);
    }

    private <T extends Task6<T>> void beforeRun(final @NonNull T task) {
        assert task != null;

        final var queue = task.queue;
        if (queue != null) {
            final var removedTask = queue.futureTasks.peek();
            if (task != removedTask || queue.scheduledTask != task) {
                throw new IllegalStateException("removedTask " + removedTask + " or queue.scheduledTask " +
                        queue.scheduledTask + " != task " + task);
            }
            queue.futureTasks.poll();
            queue.activeTask = task;
        }

        switch (task) {
            case Task6.RunnableTask runnableTask -> {
                if (futureTasks.peek() != runnableTask) {
                    throw new IllegalStateException();
                }
                // Also, start another thread if there's more work or scheduling to do.
                if (futureTasks.poll() != null) {
                    startAnotherThread();
                }
            }
            case Task6.ScheduledTask scheduledTask -> {
                scheduledTask.nextExecuteNanoTime = -1L;
                if (futureScheduledTasks.poll() != scheduledTask) {
                    throw new IllegalStateException();
                }
                // Also start another thread if there's more work or scheduling to do.
                if (!futureScheduledTasks.isEmpty()) {
                    startAnotherScheduledThread();
                }
            }
            default -> throw new IllegalStateException("Unexpected task type: " + task);
        }
    }

    private void afterScheduledRun(final Task6.@NonNull ScheduledTask task,
                                   final long delayNanos,
                                   final boolean completedNormally) {
        assert task != null;

        final var queue = (TaskQueue6.ScheduledQueue) task.queue;
        if (queue != null) {
            afterScheduledRun(task, delayNanos, queue);
        }

        // If the task crashed, start another thread to run the next task.
        if (!completedNormally && !futureScheduledTasks.isEmpty()) {
            startAnotherScheduledThread();
        }
    }

    private void afterScheduledRun(final Task6.@NonNull ScheduledTask task,
                                   final long delayNanos,
                                   final TaskQueue6.@NonNull ScheduledQueue queue) {
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
            futureScheduledTasks.offer(nextTaskInQueue);
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
    private Task6.ScheduledTask awaitScheduledTaskToRun() {
        while (true) {
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
            } else if (scheduledCoordinatorWaiting) {
                if (taskDelayNanos < scheduledCoordinatorWakeUpAt - now) {
                    coordinatorNotify();
                }
                return null;

                // No other thread is coordinating. Become the coordinator and wait for this scheduled task!
            } else {
                scheduledCoordinatorWaiting = true;
                scheduledCoordinatorWakeUpAt = now + taskDelayNanos;
                var fullyWaited = false;
                try {
                    fullyWaited = coordinatorWait(taskDelayNanos);
                } catch (InterruptedException ignored) {
                    // Will cause all tasks to exit unless more are scheduled!
                    cancelAll();
                } finally {
                    scheduledCoordinatorWaiting = false;
                }
                // wait was fully done, return this scheduled task now ready to go.
                if (fullyWaited && scheduledTask == futureScheduledTasks.peek()) {
                    beforeRun(scheduledTask);
                    return scheduledTask;
                }
            }
        }
    }


    private Task6.RunnableTask awaitTaskToRun() {
        // try to peek a runnable task
        final var task = futureTasks.peek();
        if (task == null) {
            return null;
        }

        // We have a task ready to go. Run it.
        beforeRun(task);
        return task;
    }

    /**
     * Start another thread, unless a new thread is already scheduled to start.
     */
    void startAnotherThread() {
        if (executeCallCount > runCallCount) {
            return; // A thread is still starting.
        }
        executeCallCount++;
        execute(runnable);
    }

    private void afterRun(final Task6.@NonNull RunnableTask task, final boolean completedNormally) {
        assert task != null;

        final var queue = (TaskQueue6.RunnableQueue) task.queue;
        if (queue != null) {
            afterRun(task, queue);
        }

        // If the task crashed, start another thread to run the next task.
        if (!completedNormally && !futureTasks.isEmpty()) {
            startAnotherThread();
        }
    }

    private void afterRun(final Task6.@NonNull RunnableTask task, final TaskQueue6.@NonNull RunnableQueue queue) {
        if (queue.activeTask != task) {
            throw new IllegalStateException("Task queue " + queue.name + " is not active." +
                    " queue.activeTask " + queue.activeTask + " != task " + task);
        }

        queue.activeTask = null;

        assert queue.scheduledTask == task;

        final var nextTaskInQueue = queue.futureTasks.peek();
        if (nextTaskInQueue != null) {
            futureTasks.offer(nextTaskInQueue);
            queue.scheduledTask = nextTaskInQueue;
        } else {
            queue.scheduledTask = null;
        }
    }

    @Override
    public @NonNull TaskQueue newQueue() {
        return new TaskQueue6.RunnableQueue(this, "Q" + nextQueueIndex.getAndIncrement());
    }

    @Override
    public @NonNull ScheduledTaskQueue newScheduledQueue() {
        return new TaskQueue6.ScheduledQueue(this, "Q" + nextQueueIndex.getAndIncrement());
    }

    @Override
    public void execute(final boolean cancellable, final Runnable block) {
        assert block != null;

        ensureRunning();
        final var task = new Task6.RunnableTask(null, cancellable) {
            @Override
            public void run() {
                block.run();
            }
        };

        lock.lock();
        try {
            final var wasEmpty = futureTasks.offer(task);
            if (wasEmpty) {
                startAnotherThread();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void shutdown() {
        if (isShuttingDown() ||
                !STATE.compareAndSet(this, RUNNING, SHUTDOWN_STARTED)) {
            return; // already shutting down or shutdown
        }

        tryShutdown();

        scheduledLock.lock();
        lock.lock();
        try {
            cancelAll();
        } finally {
            lock.unlock();
            scheduledLock.unlock();
        }

        try {
            if (!terminationSignal.await(1, TimeUnit.MILLISECONDS)) {
                System.out.println("interrupting remaining active threads");
                threads.forEach(Thread::interrupt);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e); // todo
        }
    }

    public long nanoTime() {
        return System.nanoTime();
    }

    public void coordinatorNotify() {
        scheduledCondition.signal();
    }

    /**
     * Wait a duration in nanoseconds.
     *
     * @return true if wait was fully completed, false if it has been signalled before ending the wait phase.
     */
    public boolean coordinatorWait(final long nanos) throws InterruptedException {
        assert nanos > 0;
        return scheduledCondition.awaitNanos(nanos) <= 0;
    }

    public void execute(final @NonNull Runnable runnable) {
        executor.execute(runnable);
    }

    private void cancelAll() {
        cancelAll(futureTasks);
        cancelAll(futureScheduledTasks);
    }

    private <T extends Task6<T>> void cancelAll(final @NonNull Queue<T> futureTasks) {
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
        if (!futureTasks.isEmpty()) {
            System.out.println("Cancelling futureTasks failed");
        }
    }
}
