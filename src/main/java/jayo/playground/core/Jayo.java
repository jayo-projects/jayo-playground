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

import jayo.playground.core.impl0.InputStreamRawReader0;
import jayo.playground.core.impl0.RealReader0;
import jayo.playground.core.impl1.InputStreamRawReader1;
import jayo.playground.core.impl1.RealReader1;
import jayo.playground.core.impl2.InputStreamRawReader2;
import jayo.playground.core.impl2.RealReader2;
import jayo.playground.core.impl3.ReadableByteChannelRawReader3;
import jayo.playground.core.impl3.RealReader3;
import jayo.playground.core.impl4.InputStreamRawReader4;
import jayo.playground.core.impl4.RealReader4;
import jayo.playground.core.impl5.InputStreamRawReader5;
import jayo.playground.core.impl5.RealReader5;
import jayo.playground.scheduling.TaskRunner;
import org.jspecify.annotations.NonNull;

import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.util.Objects;

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

    /**
     * @return a raw reader that reads from {@code in} stream.
     */
    public static @NonNull RawReader reader0(final @NonNull InputStream in) {
        Objects.requireNonNull(in);
        return new InputStreamRawReader0(in);
    }

    /**
     * @return a new reader that buffers reads from the raw {@code reader}. The returned reader will perform bulk reads
     * into its underlying buffer.
     * <p>
     * Read operations from the raw {@code reader} are processed <b>synchronously</b>.
     * <p>
     * Use this wherever you synchronously read from a raw reader to get an ergonomic and efficient access to data.
     */
    public static @NonNull Reader buffer1(final @NonNull RawReader reader) {
        return new RealReader1(reader, false);
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
    public static @NonNull Reader bufferAsync1(final @NonNull RawReader reader) {
        return new RealReader1(reader, true);
    }

    /**
     * @return a raw reader that reads from {@code in} stream.
     */
    public static @NonNull RawReader reader1(final @NonNull InputStream in) {
        Objects.requireNonNull(in);
        return new InputStreamRawReader1(in);
    }

    /**
     * @return a new reader that buffers reads from the raw {@code reader}. The returned reader will perform bulk reads
     * into its underlying buffer.
     * <p>
     * Read operations from the raw {@code reader} are processed <b>synchronously</b>.
     * <p>
     * Use this wherever you synchronously read from a raw reader to get an ergonomic and efficient access to data.
     */
    public static @NonNull Reader buffer2(final @NonNull RawReader reader) {
        Objects.requireNonNull(reader);
        return new RealReader2(reader, null);
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
    public static @NonNull Reader bufferAsync2(final @NonNull RawReader reader, final @NonNull TaskRunner taskRunner) {
        Objects.requireNonNull(reader);
        Objects.requireNonNull(taskRunner);
        return new RealReader2(reader, taskRunner);
    }

    /**
     * @return a raw reader that reads from {@code in} stream.
     */
    public static @NonNull RawReader reader2(final @NonNull InputStream in) {
        Objects.requireNonNull(in);
        return new InputStreamRawReader2(in);
    }

    /**
     * @return a new reader that buffers reads from the raw {@code reader}. The returned reader will perform bulk reads
     * into its underlying buffer.
     * <p>
     * Read operations from the raw {@code reader} are processed <b>synchronously</b>.
     * <p>
     * Use this wherever you synchronously read from a raw reader to get an ergonomic and efficient access to data.
     */
    public static @NonNull Reader buffer3(final @NonNull RawReader reader) {
        Objects.requireNonNull(reader);
        return new RealReader3(reader);
    }

    /**
     * @return a raw reader that reads from {@code in} readable byte channel.
     */
    public static @NonNull RawReader reader3(final @NonNull ReadableByteChannel in) {
        Objects.requireNonNull(in);
        return new ReadableByteChannelRawReader3(in);
    }

    /**
     * @return a new reader that buffers reads from the raw {@code reader}. The returned reader will perform bulk reads
     * into its underlying buffer.
     * <p>
     * Read operations from the raw {@code reader} are processed <b>synchronously</b>.
     * <p>
     * Use this wherever you synchronously read from a raw reader to get an ergonomic and efficient access to data.
     */
    public static @NonNull Reader buffer4(final @NonNull RawReader reader) {
        return new RealReader4(reader);
    }

    /**
     * @return a raw reader that reads from {@code in} stream.
     */
    public static @NonNull RawReader reader4(final @NonNull InputStream in) {
        Objects.requireNonNull(in);
        return new InputStreamRawReader4(in);
    }

    /**
     * @return a new reader that buffers reads from the raw {@code reader}. The returned reader will perform bulk reads
     * into its underlying buffer.
     * <p>
     * Read operations from the raw {@code reader} are processed <b>synchronously</b>.
     * <p>
     * Use this wherever you synchronously read from a raw reader to get an ergonomic and efficient access to data.
     */
    public static @NonNull Reader buffer5(final @NonNull RawReader reader) {
        return new RealReader5(reader);
    }

    /**
     * @return a raw reader that reads from {@code in} stream.
     */
    public static @NonNull RawReader reader5(final @NonNull InputStream in) {
        Objects.requireNonNull(in);
        return new InputStreamRawReader5(in);
    }
}
