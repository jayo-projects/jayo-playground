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

package jayo.playground.core;

import org.jspecify.annotations.NonNull;

import java.nio.charset.Charset;

/**
 * A reader that facilitates typed data reads and keeps a buffer internally so that callers can read chunks of data
 * without requesting it from a downstream on every call.
 */
public interface Reader extends RawReader {
    /**
     * @return the current number of bytes that can be read (or skipped over) from this reader without blocking, which
     * may be 0, or 0 when this reader is {@linkplain #exhausted() exhausted}. Ongoing or future blocking operations may
     * increase the number of available bytes.
     * <p>
     * It is never correct to use the return value of this method to allocate a buffer intended to hold all data in this
     * reader.
     * @throws JayoClosedResourceException if this reader is closed.
     */
    long bytesAvailable();

    /**
     * The call of this method will block until there are bytes to read or this reader is definitely exhausted.
     *
     * @return true if there are no more bytes in this reader.
     * @throws JayoClosedResourceException if this reader is closed.
     */
    boolean exhausted();

    /**
     * Attempts to fill the buffer with at least {@code byteCount} bytes of data from the underlying reader.
     * <p>
     * If the buffer already contains required number of bytes then there will be no requests to the underlying reader.
     *
     * @param byteCount the number of bytes that the buffer should contain.
     * @return a boolean value indicating if the requirement was successfully fulfilled. {@code false} indicates that
     * the underlying reader was exhausted before filling the buffer with {@code byteCount} bytes of data.
     * @throws IllegalArgumentException if {@code byteCount} is negative.
     * @throws IllegalStateException    if this reader is closed.
     */
    boolean request(final long byteCount);

    /**
     * Reads and discards {@code byteCount} bytes from this reader.
     *
     * @param byteCount the number of bytes to be skipped.
     * @throws JayoEOFException         if this reader is exhausted before the requested number of bytes can be
     *                                  skipped.
     * @throws IllegalArgumentException if {@code byteCount} is negative.
     * @throws IllegalStateException    if this reader is closed.
     */
    void skip(final long byteCount);

    /**
     * Attempts to fill the buffer with at least {@code byteCount} bytes of data from the underlying reader and throw
     * {@link JayoEOFException} when the reader is exhausted before fulfilling the requirement.
     * <p>
     * If the buffer already contains required number of bytes then there will be no requests to the underlying reader.
     *
     * @param byteCount the number of bytes that the buffer should contain.
     * @throws JayoEOFException         if this reader is exhausted before the required bytes count could be read.
     * @throws IllegalArgumentException if {@code byteCount} is negative.
     * @throws IllegalStateException    if this reader is closed.
     */
    void require(final long byteCount);

    /**
     * Removes all bytes from this reader, decodes them as UTF-8, and returns the string. Returns the empty string if
     * this reader is empty.
     * <pre>
     * {@code
     * Buffer buffer = Buffer.create()
     * .write("Uh uh uh!")
     * .writeByte(' ')
     * .write("You didn't say the magic word!");
     * assertThat(buffer.bytesAvailable()).isEqualTo(40);
     *
     * assertThat(buffer.readString()).isEqualTo("Uh uh uh! You didn't say the magic word!");
     * assertThat(buffer.bytesAvailable()).isEqualTo(0);
     *
     * assertThat(buffer.readString()).isEqualTo("");
     * }
     * </pre>
     *
     * @throws JayoClosedResourceException if this reader is closed.
     */
    @NonNull
    String readString();

    /**
     * Removes {@code byteCount} bytes from this reader, decodes them as UTF-8, and returns the string.
     * <pre>
     * {@code
     * Buffer buffer = Buffer.create()
     * .write("Uh uh uh!")
     * .writeByte(' ')
     * .write("You didn't say the magic word!");
     * assertThat(buffer.bytesAvailable()).isEqualTo(40);
     *
     * assertThat(buffer.readString(14)).isEqualTo("Uh uh uh! You ");
     * assertThat(buffer.bytesAvailable()).isEqualTo(26);
     *
     * assertThat(buffer.readString(14)).isEqualTo("didn't say the");
     * assertThat(buffer.bytesAvailable()).isEqualTo(12);
     *
     * assertThat(buffer.readString(12)).isEqualTo(" magic word!");
     * assertThat(buffer.bytesAvailable()).isEqualTo(0);
     * }
     * </pre>
     *
     * @param byteCount the number of bytes to read from this reader for string decoding.
     * @throws IllegalArgumentException if {@code byteCount} is negative.
     * @throws JayoEOFException         when this reader is exhausted before reading {@code byteCount} bytes from it.
     * @throws IllegalStateException    if this reader is closed.
     */
    @NonNull
    String readString(final long byteCount);

    /**
     * Removes {@code byteCount} bytes from this reader, decodes them as {@code charset}, and returns the string.
     * <pre>
     * {@code
     * Buffer buffer = Buffer.create()
     * .write("Uh uh uh¡", StandardCharsets.ISO_8859_1)
     * .writeByte(' ')
     * .write("You didn't say the magic word¡", StandardCharsets.ISO_8859_1);
     * assertThat(buffer.bytesAvailable()).isEqualTo(40);
     *
     * assertThat(buffer.readString(14, StandardCharsets.ISO_8859_1)).isEqualTo("Uh uh uh¡ You ");
     * assertThat(buffer.bytesAvailable()).isEqualTo(26);
     *
     * assertThat(buffer.readString(14, StandardCharsets.ISO_8859_1)).isEqualTo("didn't say the");
     * assertThat(buffer.bytesAvailable()).isEqualTo(12);
     *
     * assertThat(buffer.readString(12, StandardCharsets.ISO_8859_1)).isEqualTo(" magic word¡");
     * assertThat(buffer.bytesAvailable()).isEqualTo(0);
     * }
     * </pre>
     *
     * @param byteCount the number of bytes to read from this reader for string decoding.
     * @throws IllegalArgumentException if {@code byteCount} is negative.
     * @throws JayoEOFException         when this reader is exhausted before reading {@code byteCount} bytes from it.
     * @throws IllegalStateException    if this reader is closed.
     */
    @NonNull
    String readString(final long byteCount, final @NonNull Charset charset);

    /**
     * Returns a new {@link Reader} that can read data from this reader without consuming it.
     * The returned reader becomes invalid once this reader is next read or closed.
     * <p>
     * Peek could be used to lookahead and read the same data multiple times.
     * <p>
     * <pre>
     * {@code
     * Buffer buffer = Buffer.create()
     * .write("abcdefghi");
     *
     * buffer.readString(3); // returns "abc", buffer contains "defghi"
     *
     * Reader peek = buffer.peek();
     * peek.readString(3); // returns "def", buffer contains "defghi"
     * peek.readString(3); // returns "ghi", buffer contains "defghi"
     *
     * buffer.readString(3) // returns "def", buffer contains "ghi"
     * }
     * </pre>
     *
     * @throws JayoClosedResourceException if this reader is closed.
     */
    @NonNull
    Reader peek();
}
