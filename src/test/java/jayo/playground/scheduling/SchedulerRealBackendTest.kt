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

package jayo.playground.scheduling

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.offset
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit
import java.util.stream.Stream

/**
 * Integration test to confirm that [RealTaskRunner] works with a real backend. Business logic is all exercised by
 * [TaskRunnerTest].
 *
 * This test is doing real sleeping with tolerances of 250 ms. Hopefully that's enough for even the busiest of CI
 * servers.
 */
class SchedulerRealBackendTest {
    companion object {
        @JvmStatic
        private val log = LinkedBlockingDeque<String>()

        @JvmStatic
        private val loggingUncaughtExceptionHandler =
            UncaughtExceptionHandler { _, throwable ->
                log.put("uncaught exception: $throwable")
            }

        @JvmStatic
        private val threadFactory = Thread.ofVirtual()
            .uncaughtExceptionHandler(loggingUncaughtExceptionHandler)
            .factory()

        @JvmStatic
        private fun parameters() =
            Stream.of<Arguments>(
                Arguments.of(Scheduler.create1(Executors.newThreadPerTaskExecutor(threadFactory))),
                Arguments.of(Scheduler.create2(Executors.newThreadPerTaskExecutor(threadFactory))),
            )
    }

    @ParameterizedTest
    @MethodSource("parameters")
    fun testQueueSchedule(scheduler: Scheduler) {
        val queue = scheduler.newQueue()
        val t1 = System.nanoTime() / 1e6

        val delays = mutableListOf(TimeUnit.MILLISECONDS.toNanos(1000), -1L)
        queue.schedule("task", TimeUnit.MILLISECONDS.toNanos(750)) {
            log.put("runOnce delays.size=${delays.size}")
            return@schedule delays.removeAt(0)
        }

        assertThat(log.take()).isEqualTo("runOnce delays.size=2")
        val t2 = System.nanoTime() / 1e6 - t1
        assertThat(t2).isCloseTo(750.0, offset(250.0))

        assertThat(log.take()).isEqualTo("runOnce delays.size=1")
        val t3 = System.nanoTime() / 1e6 - t1
        assertThat(t3).isCloseTo(1750.0, offset(250.0))
    }

    @ParameterizedTest
    @MethodSource("parameters")
    fun testQueueExecute(scheduler: Scheduler) {
        val queue = scheduler.newQueue()
        val t1 = System.nanoTime() / 1e6

        queue.execute("task", true) {
            log.put("runOnce")
        }

        assertThat(log.take()).isEqualTo("runOnce")
        val t2 = System.nanoTime() / 1e6 - t1
        assertThat(t2).isCloseTo(0.0, offset(250.0))
    }

    @ParameterizedTest
    @MethodSource("parameters")
    fun testSingleExecute(scheduler: Scheduler) {
        val t1 = System.nanoTime() / 1e6

        scheduler.execute(true) {
            log.put("runOnce")
        }

        assertThat(log.take()).isEqualTo("runOnce")
        val t2 = System.nanoTime() / 1e6 - t1
        assertThat(t2).isCloseTo(0.0, offset(250.0))
    }

    @ParameterizedTest
    @MethodSource("parameters")
    fun taskFailsWithUncheckedException(scheduler: Scheduler) {
        val queue = scheduler.newQueue()
        queue.schedule("task", TimeUnit.MILLISECONDS.toNanos(100)) {
            log.put("failing task running")
            throw RuntimeException("boom!")
        }

        queue.schedule("task", TimeUnit.MILLISECONDS.toNanos(200)) {
            log.put("normal task running")
            return@schedule -1L
        }

        queue.idleLatch().await(500, TimeUnit.MILLISECONDS)

        assertThat(log.take()).isEqualTo("failing task running")
        assertThat(log.take()).isEqualTo("uncaught exception: java.lang.RuntimeException: boom!")
        assertThat(log.take()).isEqualTo("normal task running")
        assertThat(log).isEmpty()
    }

    @ParameterizedTest
    @MethodSource("parameters")
    fun idleLatchAfterShutdown(scheduler: Scheduler) {
        val queue = scheduler.newQueue()
        queue.schedule("task", 0L) {
            Thread.sleep(250)
            scheduler.shutdown()
            return@schedule -1L
        }

        assertThat(queue.idleLatch().count).isEqualTo(1)
        assertThat(queue.idleLatch().await(500L, TimeUnit.MILLISECONDS)).isTrue()
        assertThat(queue.idleLatch().count).isEqualTo(0)
    }
}
