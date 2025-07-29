/*
 * Copyright (c) 2024-present, pull-vert and Jayo contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 *
 * Forked from Okio (https://github.com/square/okio) and kotlinx-io (https://github.com/Kotlin/kotlinx-io), original
 * copyrights are below
 *
 * Copyright 2017-2023 JetBrains s.r.o. and respective authors and developers.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENCE file.
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

package jayo.playground.core

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test

class RealAsyncReaderTest : AbstractReaderTest(ReaderFactory.REAL_ASYNC_SOURCE)

class BufferReaderTest : AbstractReaderTest(ReaderFactory.BUFFER)

class BufferedReaderTest : AbstractReaderTest(ReaderFactory.BUFFERED_SOURCE)

class RealReaderTest : AbstractReaderTest(ReaderFactory.REAL_SOURCE)

class PeekBufferTest : AbstractReaderTest(ReaderFactory.PEEK_BUFFER)

class PeekReaderTest : AbstractReaderTest(ReaderFactory.PEEK_SOURCE)

class PeekAsyncReaderTest : AbstractReaderTest(ReaderFactory.PEEK_ASYNC_SOURCE)

abstract class AbstractReaderTest internal constructor(private val factory: ReaderFactory) {
    companion object {
        const val SEGMENT_SIZE = 16_709

        const val UTF8 = "Սｍ, I'll 𝓽𝖾ll ᶌօ𝘂 ᴛℎ℮ 𝜚𝕣०ｂl𝖾ｍ ｗі𝕥𝒽 𝘵𝘩𝐞 𝓼𝙘𝐢𝔢𝓷𝗍𝜄𝚏𝑖ｃ 𝛠𝝾ｗ𝚎𝑟 𝕥ｈ⍺𝞃 𝛄𝓸𝘂'𝒓𝗲 υ𝖘𝓲𝗇ɡ 𝕙𝚎𝑟ｅ"
    }

    private lateinit var writer: Buffer
    private lateinit var reader: Reader

    @BeforeEach
    fun before() {
        val pipe = factory.pipe()
        writer = pipe.writer
        reader = pipe.reader
    }

    @AfterEach
    fun after() {
        try {
            reader.close()
            writer.close()
        } catch (e: Exception) { /*ignored*/
            e.printStackTrace()
        }
    }

    @Test
    fun exhausted() {
        assertThat(reader.exhausted()).isTrue()
    }

    // this test is a good race-condition test, do it several times!
    @RepeatedTest(50)
    fun longHexAlphabet() {
        writer.write("7896543210abcdef")
        assertThat(reader.readHexadecimalUnsignedLong()).isEqualTo(0x7896543210abcdefL)
        writer.write("ABCDEF")
        assertThat(reader.readHexadecimalUnsignedLong()).isEqualTo(0xabcdefL)
    }

    @Test
    fun readUtf8() {
        writer.write(UTF8)
        assertThat(reader.readString()).isEqualTo(UTF8)
    }

    @RepeatedTest(2)
    open fun readUtf8StringSpansSegments() {
        writer.write("a".repeat(SEGMENT_SIZE * 2))
        reader.skip((SEGMENT_SIZE - 1).toLong())
        assertThat(reader.readString(2)).isEqualTo("aa")
    }

    @Test
    fun readUtf8StringSegment() {
        writer.write("a".repeat(SEGMENT_SIZE))
        assertThat(reader.readString(SEGMENT_SIZE.toLong())).isEqualTo("a".repeat(SEGMENT_SIZE))
    }

    @Test
    fun readUtf8StringPartialBuffer() {
        writer.write("a".repeat(SEGMENT_SIZE + 20))
        assertThat(reader.readString((SEGMENT_SIZE + 10).toLong())).isEqualTo("a".repeat(SEGMENT_SIZE + 10))
    }

    @Test
    open fun readUtf8StringEntireBuffer() {
        writer.write("a".repeat(SEGMENT_SIZE * 2))
        assertThat(reader.readString()).isEqualTo("a".repeat(SEGMENT_SIZE * 2))
    }

    @Test
    fun readUtf8StringTooShortThrows() {
        writer.write("abc")
        assertThatThrownBy {
            reader.readString(4L)
        }.isInstanceOf(JayoEOFException::class.java)

        assertThat(reader.readString()).isEqualTo("abc") // The read shouldn't consume any data.
    }

    @Test
    fun skipInsufficientData() {
        writer.write("a")
        assertThatThrownBy {
            reader.skip(2)
        }.isInstanceOf(JayoEOFException::class.java)
    }
}
