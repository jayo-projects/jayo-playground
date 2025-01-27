package jayo.playground.benchmarks

import jayo.playground.scheduling.Scheduler
import jayo.playground.scheduling.TaskQueue
import org.openjdk.jmh.annotations.*
import java.util.concurrent.*

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@Timeout(time = 20) // in seconds
@Warmup(iterations = 7, time = 1) // in seconds
@Measurement(iterations = 5, time = 1) // in seconds
@BenchmarkMode(Mode.Throughput)
@Fork(value = 1)
open class SchedulerBenchmark {
    @Param("1", "2")
    private var schedulerVersion = 0

    @Param("virtual", "pooled")
    private lateinit var executorType: String

    private lateinit var executor: ExecutorService
    private lateinit var latch: CountDownLatch

    private lateinit var scheduler: Scheduler
    private lateinit var taskQueue1: TaskQueue
    private lateinit var taskQueue2: TaskQueue
    private lateinit var taskQueue3: TaskQueue
    private lateinit var taskQueue4: TaskQueue
    private lateinit var taskQueue5: TaskQueue
    private lateinit var taskQueue6: TaskQueue

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


        scheduler = when (schedulerVersion) {
            1 -> Scheduler.create1(executor)
            2 -> Scheduler.create2(executor)
            else -> throw IllegalStateException("Unknown scheduler version: $schedulerVersion")
        }

        taskQueue1 = scheduler.newQueue()
        taskQueue2 = scheduler.newQueue()
        taskQueue3 = scheduler.newQueue()
        taskQueue4 = scheduler.newQueue()
        taskQueue5 = scheduler.newQueue()
        taskQueue6 = scheduler.newQueue()

        latch = CountDownLatch(OPERATIONS_PER_INVOCATION)
    }

    @TearDown(Level.Invocation)
    fun tearDown() {
        taskQueue1.shutdownNow()
        taskQueue2.shutdownNow()
        taskQueue3.shutdownNow()
        taskQueue4.shutdownNow()
        taskQueue5.shutdownNow()
        taskQueue6.shutdownNow()
        executor.shutdownNow()
    }

    @Benchmark
    fun taskRunner1Execute() {
        repeat(OPERATIONS_PER_INVOCATION) {
            scheduler.execute(true) { immediateRunnable(latch) }
        }
        latch.await(20, TimeUnit.SECONDS)
    }

    @Benchmark
    fun taskRunner2QueueExecute() {
        var index = OPERATIONS_PER_INVOCATION / 6
        while (index >= 0) {
            taskQueue1.execute("queue1-${index}", true) { immediateRunnable(latch) }
            taskQueue2.execute("queue2-${index}", true) { immediateRunnable(latch) }
            taskQueue3.execute("queue3-${index}", true) { immediateRunnable(latch) }
            taskQueue4.execute("queue4-${index}", true) { immediateRunnable(latch) }
            taskQueue5.execute("queue3-${index}", true) { immediateRunnable(latch) }
            taskQueue6.execute("queue4-${index}", true) { immediateRunnable(latch) }
            index--
        }
        latch.await(20, TimeUnit.SECONDS)
    }

    @Benchmark
    fun taskRunner3QueueSchedule() {
        taskQueue1.schedule("queue1", DELAY) { schedule(latch) }
        taskQueue2.schedule("queue2", DELAY) { schedule(latch) }
        taskQueue3.schedule("queue3", DELAY) { schedule(latch) }
        taskQueue4.schedule("queue4", DELAY) { schedule(latch) }
        taskQueue5.schedule("queue3", DELAY) { schedule(latch) }
        taskQueue6.schedule("queue4", DELAY) { schedule(latch) }
        latch.await(20, TimeUnit.SECONDS)
    }

    @Benchmark
    fun taskRunner4Mixed() {
        taskQueue1.schedule("queue1", DELAY) { schedule(latch) }
        taskQueue2.schedule("queue2", DELAY) { schedule(latch) }
        var index = OPERATIONS_PER_INVOCATION / 6
        while (index >= 0) {
            taskQueue3.execute("queue3-${index}", true) { immediateRunnable(latch) }
            scheduler.execute(true) { immediateRunnable(latch) }
            taskQueue4.execute("queue4-${index}", true) { immediateRunnable(latch) }
            scheduler.execute(true) { immediateRunnable(latch) }
            index--
        }
        latch.await(20, TimeUnit.SECONDS)
    }
}