/*
 * Copyright (c) 2024-present, pull-vert and Jayo contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package jayo.playground.core

import jayo.playground.core.AbstractReaderTest.Companion.SEGMENT_SIZE
import jayo.playground.core.JavaVersionUtils.executorService
import jayo.playground.scheduling.TaskRunner
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.RepeatedTest
import java.io.InputStream
import kotlin.random.Random

// these tests are a good race-condition test, do them several times!
class ReaderAsyncTests {
    companion object {
        private const val CHUNKS = 16
        const val CHUNKS_BYTE_SIZE = 4 * SEGMENT_SIZE
        const val EXPECTED_SIZE = CHUNKS * CHUNKS_BYTE_SIZE
        val ARRAY = ByteArray(CHUNKS_BYTE_SIZE) { 0x61 }

        val TASK_RUNNER: TaskRunner = TaskRunner.create5(executorService())
    }

    @RepeatedTest(10)
    fun readerSlowProducerFastConsumer() {
        val inputStream: InputStream = inputStream(true)

        Jayo.buffer2(Jayo.reader2(inputStream)).use { reader ->
            assertThat(reader.readString()).isEqualTo("a".repeat(EXPECTED_SIZE))
        }
    }

    @RepeatedTest(10)
    fun asyncReaderSlowProducerFastConsumer() {
        val inputStream: InputStream = inputStream(true)

        Jayo.bufferAsync2(Jayo.reader2(inputStream), TASK_RUNNER).use { reader ->
            assertThat(reader.readString()).isEqualTo("a".repeat(EXPECTED_SIZE))
        }
    }

    @RepeatedTest(10)
    fun readerFastProducerSlowConsumer() {
        val inputStream: InputStream = inputStream(false)

        var result = ""
        var offset = 0
        Jayo.buffer2(Jayo.reader2(inputStream)).use { reader ->
            while (offset < EXPECTED_SIZE) {
                Thread.sleep(0, Random.nextInt(5) /*in nanos*/)
                result += reader.readString((CHUNKS_BYTE_SIZE * 2).toLong())
                offset += CHUNKS_BYTE_SIZE * 2
            }
            assertThat(result).hasSize(EXPECTED_SIZE)
            assertThat(result).isEqualTo("a".repeat(EXPECTED_SIZE))
        }
    }

    @RepeatedTest(10)
    fun asyncReaderFastProducerSlowConsumer() {
        val inputStream: InputStream = inputStream(false)

        var result = ""
        var offset = 0
        Jayo.bufferAsync2(Jayo.reader2(inputStream), TASK_RUNNER).use { reader ->
            while (offset < EXPECTED_SIZE) {
                Thread.sleep(0, Random.nextInt(5) /*in nanos*/)
                result += reader.readString((CHUNKS_BYTE_SIZE * 2).toLong())
                offset += CHUNKS_BYTE_SIZE * 2
            }
            assertThat(result).hasSize(EXPECTED_SIZE)
            assertThat(result).isEqualTo("a".repeat(EXPECTED_SIZE))
        }
    }

    @RepeatedTest(10)
    fun readerSlowProducerSlowConsumer() {
        val inputStream: InputStream = inputStream(true)

        var result = ""
        var offset = 0
        Jayo.buffer2(Jayo.reader2(inputStream)).use { reader ->
            while (offset < EXPECTED_SIZE) {
                Thread.sleep(0, Random.nextInt(5) /*in nanos*/)
                result += reader.readString((CHUNKS_BYTE_SIZE / 2).toLong())
                offset += CHUNKS_BYTE_SIZE / 2
            }
            assertThat(result).hasSize(EXPECTED_SIZE)
            assertThat(result).isEqualTo("a".repeat(EXPECTED_SIZE))
        }
    }

    @RepeatedTest(30)
    fun asyncReaderSlowProducerSlowConsumer() {
        val inputStream: InputStream = inputStream(true)

        var result = ""
        var offset = 0
        Jayo.buffer5(Jayo.reader5(inputStream)).use { reader ->
            while (offset < EXPECTED_SIZE) {
                Thread.sleep(0, Random.nextInt(5) /*in nanos*/)
                result += reader.readString((CHUNKS_BYTE_SIZE / 2).toLong())
                offset += CHUNKS_BYTE_SIZE / 2
            }
            assertThat(result).hasSize(EXPECTED_SIZE)
            assertThat(result).isEqualTo("a".repeat(EXPECTED_SIZE))
        }
    }

    private fun inputStream(delayed: Boolean) = object : InputStream() {
        var sent = 0

        override fun read(): Int {
            throw Exception("Purposely not implemented")
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (sent >= EXPECTED_SIZE) {
                return -1
            }
            if (delayed) {
                Thread.sleep(0, Random.nextInt(5) /*in nanos*/)
            }
            val toWrite = kotlin.comparisons.minOf(len, CHUNKS_BYTE_SIZE)
            ARRAY.copyInto(b, off, 0, toWrite)
            sent += toWrite
            return toWrite
        }
    }
}