package jayo.playground.benchmarks

import jayo.playground.scheduling.TaskRunner
import org.openjdk.jmh.annotations.*
import java.util.concurrent.*

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@Timeout(time = 20) // in seconds
@Warmup(iterations = 7, time = 1) // in seconds
@Measurement(iterations = 5, time = 1) // in seconds
@BenchmarkMode(Mode.Throughput)
@Fork(value = 1)
open class TaskRunnerBenchmark {
    @Param("1", "2", "3")
    private var schedulerVersion = 0

    @Param("virtual"/*, "pooled"*/)
    private lateinit var executorType: String

    private lateinit var executor: ExecutorService
    private lateinit var latch: CountDownLatch

    private lateinit var taskRunner: TaskRunner

    companion object {
        private const val OPERATIONS_PER_INVOCATION = 1200
        private const val DELAY = 1000L

        @JvmStatic
        private fun immediateRunnable(countDownLatch: CountDownLatch) {
            countDownLatch.countDown()
        }

        @JvmStatic
        private fun schedule(countDownLatch: CountDownLatch): Long {
            countDownLatch.countDown()
            return DELAY
        }
    }

    @Setup(Level.Invocation)
    fun setup() {
        executor = when (executorType) {
            "virtual" -> Executors.newVirtualThreadPerTaskExecutor()
            "pooled" -> {
                ThreadPoolExecutor(
                    // corePoolSize:
                    4,
                    // maximumPoolSize:
                    4,
                    // keepAliveTime:
                    60L,
                    TimeUnit.MINUTES,
                    LinkedBlockingQueue(),
                    Thread.ofVirtual().factory(),
                ).apply { prestartAllCoreThreads() }
            }

            else -> throw IllegalStateException("Unknown executor type: $executorType")
        }
        executor = Executors.newVirtualThreadPerTaskExecutor()


        taskRunner = when (schedulerVersion) {
            1 -> TaskRunner.create1(executor)
            2 -> TaskRunner.create2(executor)
            3 -> TaskRunner.create3(executor)
            else -> throw IllegalStateException("Unknown scheduler version: $schedulerVersion")
        }

        latch = CountDownLatch(OPERATIONS_PER_INVOCATION)
    }

    @TearDown(Level.Invocation)
    fun tearDown() {
        taskRunner.shutdown()
    }

    @Benchmark
    fun taskRunner1Execute() {
        repeat(OPERATIONS_PER_INVOCATION) {
            taskRunner.execute(true) { immediateRunnable(latch) }
        }
        latch.await(20, TimeUnit.SECONDS)
    }

    @Benchmark
    fun taskRunner2QueueExecute() {
        var index = OPERATIONS_PER_INVOCATION / 6
        val taskQueue1 = taskRunner.newQueue()
        val taskQueue2 = taskRunner.newQueue()
        val taskQueue3 = taskRunner.newQueue()
        val taskQueue4 = taskRunner.newQueue()
        val taskQueue5 = taskRunner.newQueue()
        val taskQueue6 = taskRunner.newQueue()

        while (index >= 0) {
            taskQueue1.execute("queue1-task${index}", true) { immediateRunnable(latch) }
            taskQueue2.execute("queue2-task${index}", true) { immediateRunnable(latch) }
            taskQueue3.execute("queue3-task${index}", true) { immediateRunnable(latch) }
            taskQueue4.execute("queue4-task${index}", true) { immediateRunnable(latch) }
            taskQueue5.execute("queue3-task${index}", true) { immediateRunnable(latch) }
            taskQueue6.execute("queue4-task${index}", true) { immediateRunnable(latch) }
            index--
        }
        latch.await(20, TimeUnit.SECONDS)

        taskQueue1.shutdown()
        taskQueue2.shutdown()
        taskQueue3.shutdown()
        taskQueue4.shutdown()
        taskQueue5.shutdown()
        taskQueue6.shutdown()
    }

    @Benchmark
    fun taskRunner3QueueSchedule() {
        val taskQueue1 = taskRunner.newScheduledQueue()
        val taskQueue2 = taskRunner.newScheduledQueue()
        val taskQueue3 = taskRunner.newScheduledQueue()
        val taskQueue4 = taskRunner.newScheduledQueue()
        val taskQueue5 = taskRunner.newScheduledQueue()
        val taskQueue6 = taskRunner.newScheduledQueue()

        taskQueue1.schedule("queue1-task", DELAY) { schedule(latch) }
        taskQueue2.schedule("queue2-task", DELAY) { schedule(latch) }
        taskQueue3.schedule("queue3-task", DELAY) { schedule(latch) }
        taskQueue4.schedule("queue4-task", DELAY) { schedule(latch) }
        taskQueue5.schedule("queue3-task", DELAY) { schedule(latch) }
        taskQueue6.schedule("queue4-task", DELAY) { schedule(latch) }
        latch.await(20, TimeUnit.SECONDS)

        taskQueue1.shutdown()
        taskQueue2.shutdown()
        taskQueue3.shutdown()
        taskQueue4.shutdown()
        taskQueue5.shutdown()
        taskQueue6.shutdown()
    }

    @Benchmark
    fun taskRunner4Mixed() {
        val taskQueue1 = taskRunner.newQueue()
        val taskQueue2 = taskRunner.newQueue()
        val taskQueue3 = taskRunner.newScheduledQueue()
        val taskQueue4 = taskRunner.newScheduledQueue()

        taskQueue3.schedule("queue3-task", DELAY) { schedule(latch) }
        taskQueue4.schedule("queue4-task", DELAY) { schedule(latch) }
        var index = OPERATIONS_PER_INVOCATION / 6
        while (index >= 0) {
            taskQueue1.execute("queue1-task${index}", true) { immediateRunnable(latch) }
            taskRunner.execute(true) { immediateRunnable(latch) }
            taskQueue2.execute("queue2-task${index}", true) { immediateRunnable(latch) }
            taskRunner.execute(true) { immediateRunnable(latch) }
            index--
        }
        latch.await(20, TimeUnit.SECONDS)

        taskQueue1.shutdown()
        taskQueue2.shutdown()
        taskQueue3.shutdown()
        taskQueue4.shutdown()
    }
}