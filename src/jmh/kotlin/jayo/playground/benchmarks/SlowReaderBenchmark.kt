package jayo.playground.benchmarks

import jayo.playground.core.JavaVersionUtils.executorService
import jayo.playground.core.Jayo
import jayo.playground.core.Reader
import jayo.playground.scheduling.TaskRunner
import org.openjdk.jmh.annotations.*
import java.io.InputStream
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@Timeout(time = 20)
@Warmup(iterations = 7, time = 1)
@Measurement(iterations = 5, time = 1)
@BenchmarkMode(Mode.Throughput)
@Fork(value = 1)
open class SlowReaderBenchmark {
    @Param("0", "1", "2")
    private var readerVersion = 0

    companion object {
        private const val CHUNKS = 256
        private const val CHUNKS_BYTE_SIZE = 1024
        private val STRING = "a".repeat(CHUNKS_BYTE_SIZE)
        private val ARRAY = ByteArray(CHUNKS_BYTE_SIZE) { 0x61 }

        val TASK_RUNNER: TaskRunner = TaskRunner.create5(executorService())
    }

    private lateinit var jayoReader: Reader

    @Setup
    fun setup() {
        val delayedInputStream = object : InputStream() {
            override fun read(): Int {
                throw Exception("Purposely not implemented")
            }

            override fun read(b: ByteArray, off: Int, len: Int): Int {
                randomSleep()
                val toRead = minOf(len, CHUNKS_BYTE_SIZE)
                ARRAY.copyInto(b, off, 0, toRead)
                return toRead
            }
        }

        when (readerVersion) {
            0 -> {
                jayoReader = Jayo.bufferAsync0(Jayo.reader0(delayedInputStream))
            }

            1 -> {
                jayoReader = Jayo.bufferAsync1(Jayo.reader1(delayedInputStream))
            }

            2 -> {
                jayoReader = Jayo.bufferAsync2(Jayo.reader2(delayedInputStream), TASK_RUNNER)
            }

            else -> throw IllegalStateException("Unknown reader version: $jayoReader")
        }
    }

    @TearDown
    fun tearDown() {
        jayoReader.close()
    }

    private fun randomSleep() {
        Thread.sleep(Duration.ofMillis(Random.nextLong(1L, 5L)))
    }

    @Benchmark
    fun readerJayo() {
        IntRange(0, CHUNKS).forEach { _ ->
            randomSleep()
            check(jayoReader.readString(CHUNKS_BYTE_SIZE.toLong()).contentEquals(STRING))
        }
    }
}