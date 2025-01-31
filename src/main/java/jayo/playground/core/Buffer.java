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

import jayo.playground.core.impl0.RealBuffer0;
import jayo.playground.core.impl1.RealBuffer1;
import org.jspecify.annotations.NonNull;

/**
 * A collection of bytes in memory.
 * <p>
 * The buffer can be viewed as an unbound queue whose size grows with the data being written and shrinks with data being
 * consumed.
 * <p>
 * Internally, the buffer consists of a queue of data segments, and the buffer's capacity grows and shrinks in units of
 * data segments instead of individual bytes. Each data segment store binary data in a fixed-sized {@code byte[]}.
 * <ul>
 * <li><b>Moving data from one buffer to another is fast.</b> The buffer was designed to reduce memory allocations when
 * possible. Instead of copying bytes from one place in memory to another, this class just changes ownership of the
 * underlying data segments.
 * <li><b>This buffer grows with your data.</b> Just like an {@code ArrayList}, each buffer starts small. It consumes
 * only the memory it needs to.
 * <li><b>This buffer pools its byte arrays.</b> When you allocate a byte array in Java, the runtime must zero-fill the
 * requested array before returning it to you. Even if you're going to write over that space anyway. This class avoids
 * zero-fill and GC churn by pooling byte arrays.
 * </ul>
 * Read and write methods on numbers use the big-endian order. If you need little-endian order, use
 * <i>reverseBytes()</i>, for example {@code Short.reverseBytes(buffer.readShort())} and
 * {@code buffer.writeShort(Short.reverseBytes(myShortValue))}. Jayo provides Kotlin extension functions that support
 * little-endian and unsigned numbers.
 */
public interface Buffer extends Reader, Writer, Cloneable {
    /**
     * @return a new {@link Buffer}
     */
    static @NonNull Buffer create0() {
        return new RealBuffer0();
    }

    /**
     * @return a new {@link Buffer}
     */
    static @NonNull Buffer create1() {
        return new RealBuffer1();
    }

    /**
     * @return the current number of bytes that can be read (or skipped over) from this buffer, which may be 0. Ongoing
     * or future write operations may increase the number of available bytes.
     */
    @Override
    long bytesAvailable();

    /**
     * This method does not affect this buffer's content as there is no upstream to write data to.
     */
    @Override
    void flush();

    /**
     * Discards all bytes in this buffer.
     * <p>
     * Call to this method is equivalent to {@link #skip(long)} with {@code byteCount = buffer.bytesAvailable()}, call
     * this method when you're done with a buffer, its segments will return to the pool.
     */
    void clear();

    /**
     * Discards {@code byteCount} bytes, starting from the head of this buffer.
     *
     * @throws IllegalArgumentException if {@code byteCount} is negative.
     */
    @Override
    void skip(final long byteCount);

    /**
     * Copy {@code byteCount} bytes from this buffer, starting at {@code offset}, to {@code out} buffer. This method
     * does not consume data from this buffer.
     *
     * @param out       the destination buffer to copy data into.
     * @param offset    the start offset (inclusive) in this buffer of the first byte to copy.
     * @param byteCount the number of bytes to copy.
     * @return {@code this}
     * @throws IndexOutOfBoundsException if {@code offset} or {@code byteCount} is out of this buffer bounds
     *                                   ({@code [0..buffer.bytesAvailable())}).
     */
    @NonNull
    Buffer copyTo(final @NonNull Buffer out,
                  final long offset,
                  final long byteCount);

    @Override
    @NonNull
    Buffer write(final @NonNull CharSequence charSequence);

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IllegalArgumentException  {@inheritDoc}
     */
    @Override
    @NonNull
    Buffer write(final @NonNull CharSequence charSequence,
                 final int startIndex,
                 final int endIndex);
}
