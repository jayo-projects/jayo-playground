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

import java.io.Closeable;

/**
 * Supplies a stream of bytes. RawReader is a base interface for Jayo data suppliers.
 * <p>
 * This interface should be implemented to read data from wherever it's located: from the network, storage, or a buffer
 * in memory. Readers may be layered to transform supplied data, such as to decompress, decrypt, or remove protocol
 * framing.
 */
public interface RawReader extends Closeable {
    /**
     * Removes at least 1, and up to {@code byteCount} bytes from this and appends them to {@code destination}.
     *
     * @param destination the destination to write the data from this reader.
     * @param byteCount   the number of bytes to read.
     * @return the number of bytes read, or {@code -1L} if this reader is exhausted.
     * @throws IllegalArgumentException    if {@code byteCount} is negative.
     * @throws JayoClosedResourceException if this reader is closed.
     * @throws JayoException               if an I/O error occurs.
     */
    long readAtMostTo(final @NonNull Buffer destination, final long byteCount);

    /**
     * Closes this reader and releases the resources held by this reader. Trying to read in a closed reader will throw a
     * {@link JayoClosedResourceException}. It is safe to close a reader more than once.
     *
     * @throws JayoException if an I/O error occurs.
     */
    @Override
    void close();
}
