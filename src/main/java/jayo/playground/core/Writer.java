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

/**
 * A writer that facilitates typed data writes and keeps a buffer internally so that caller can write some data without
 * sending it directly to an upstream.
 * <p>
 * Writer is the main Jayo interface to write data in client's code, any {@link RawWriter} could be turned into
 * {@link Writer} using {@code Jayo.buffer(myRawWriter)}.
 * <p>
 * Depending on the kind of upstream and the number of bytes written, buffering may improve the performance by hiding
 * the latency of small writes.
 */
public interface Writer extends RawWriter {

    /**
     * Encodes all the characters from {@code charSequence} using UTF-8 and writes them to this writer.
     * <pre>
     * {@code
     * Buffer buffer = Buffer.create();
     * buffer.write("Uh uh uh!");
     * buffer.writeByte(' ');
     * buffer.write("You didn't say the magic word!");
     *
     * assertThat(buffer.readString()).isEqualTo("Uh uh uh! You didn't say the magic word!");
     * }
     * </pre>
     *
     * @param charSequence the char sequence to be encoded.
     * @return {@code this}
     * @throws JayoClosedResourceException if this writer is closed.
     */
    @NonNull
    Writer write(final @NonNull CharSequence charSequence);

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IllegalArgumentException  {@inheritDoc}
     */
    @NonNull
    Writer write(final @NonNull CharSequence charSequence,
                 final int startIndex,
                 final int endIndex);
}
