/*
 * Copyright (c) 2024-present, pull-vert and Jayo contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 *
 * Forked from Okio (https://github.com/square/okio), original copyright is below
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

import jayo.playground.core.impl0.RealReader0;
import org.jspecify.annotations.NonNull;

/**
 * Essential APIs for working with Jayo.
 */
public final class Jayo {
    // un-instantiable
    private Jayo() {
    }

    /**
     * @return a new reader that buffers reads from the raw {@code reader}. The returned reader will perform bulk reads
     * into its underlying buffer.
     * <p>
     * Read operations from the raw {@code reader} are processed <b>synchronously</b>.
     * <p>
     * Use this wherever you synchronously read from a raw reader to get an ergonomic and efficient access to data.
     */
    public static @NonNull Reader buffer0(final @NonNull RawReader reader) {
        return new RealReader0(reader, false);
    }

    /**
     * @return a new reader that buffers reads from the raw {@code reader}. The returned reader will perform bulk reads
     * into its underlying buffer.
     * <p>
     * Read operations from the raw {@code reader} are seamlessly processed <b>asynchronously</b> by a virtual
     * thread.
     * <p>
     * Use this wherever you asynchronously read from a raw reader to get an ergonomic and efficient access to data.
     */
    public static @NonNull Reader bufferAsync0(final @NonNull RawReader reader) {
        return new RealReader0(reader, true);
    }
}
