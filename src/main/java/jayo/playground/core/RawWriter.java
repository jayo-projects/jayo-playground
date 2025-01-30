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
import java.io.Flushable;

/**
 * Receives a stream of bytes. RawWriter is a base interface for Jayo data receivers.
 * <p>
 * This interface should be implemented to write data wherever it's needed: to the network, storage, or a buffer in
 * memory. Writers may be layered to transform received data, such as to compress, encrypt, throttle, or add protocol
 * framing.
 */
public interface RawWriter extends Closeable, Flushable {
    /**
     * Removes {@code byteCount} bytes from {@code reader} and appends them to this writer.
     *
     * @param reader    the reader to read data from.
     * @param byteCount the number of bytes to write.
     * @throws IndexOutOfBoundsException   if the {@code reader}'s size is below {@code byteCount} or {@code byteCount}
     *                                     is negative.
     * @throws JayoException               if an I/O error occurs.
     */
    void write(final @NonNull Buffer reader, final long byteCount);

    /**
     * Pushes all buffered bytes to their final destination.
     *
     * @throws JayoClosedResourceException if this writer is closed.
     * @throws JayoException               if an I/O error occurs.
     */
    @Override
    void flush();

    /**
     * Pushes all buffered bytes to their final destination and releases the resources held by this writer. Trying to
     * write to a closed writer will throw a {@link JayoClosedResourceException}. It is safe to close a writer more than
     * once.
     *
     * @throws JayoException if an I/O error occurs.
     */
    @Override
    void close();
}
