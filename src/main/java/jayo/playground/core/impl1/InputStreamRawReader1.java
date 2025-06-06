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

package jayo.playground.core.impl1;

import jayo.playground.core.Buffer;
import jayo.playground.core.JayoException;
import jayo.playground.core.RawReader;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static java.lang.System.Logger.Level.TRACE;

public final class InputStreamRawReader1 implements RawReader {
    private static final System.Logger LOGGER = System.getLogger("jayo.InputStreamRawReader");

    private final @NonNull InputStream in;

    public InputStreamRawReader1(final @NonNull InputStream in) {
        this.in = Objects.requireNonNull(in);
    }

    /**
     * Execute a single read from the InputStream, that reads up to byteCount bytes of data from the input stream.
     * A smaller number may be read.
     *
     * @return the number of bytes actually read.
     */
    @Override
    public long readAtMostTo(final @NonNull Buffer destination, final long byteCount) {
        Objects.requireNonNull(destination);
        if (byteCount < 0L) {
            throw new IllegalArgumentException("byteCount < 0 : " + byteCount);
        }
        if (!(destination instanceof RealBuffer1 _writer)) {
            throw new IllegalArgumentException("writer must be an instance of RealBuffer");
        }

        if (byteCount == 0L) {
            return 0L;
        }

        if (LOGGER.isLoggable(TRACE)) {
            LOGGER.log(TRACE, "InputStreamRawReader: Start reading up to {0} bytes from the InputStream to " +
                            "{1}Buffer(SegmentQueue={2}){3}",
                    byteCount, System.lineSeparator(), _writer.segmentQueue, System.lineSeparator());
        }

        final var bytesRead = _writer.segmentQueue.withWritableTail(1, (tail) -> {
            final var limit = tail.limit;
            final var toRead = (int) Math.min(byteCount, Segment.SIZE - limit);
            final int read;
            try {
                read = in.read(tail.data, limit, toRead);
            } catch (IOException e) {
                throw JayoException.buildJayoException(e);
            }
            if (read > 0) {
                tail.limit += read;
            }
            return read;
        });

        if (LOGGER.isLoggable(TRACE)) {
            LOGGER.log(TRACE, "InputStreamRawReader: Finished reading {0}/{1} bytes from the InputStream to " +
                            "{2}Buffer(SegmentQueue={3}){4}",
                    bytesRead, byteCount, System.lineSeparator(), _writer.segmentQueue, System.lineSeparator());
        }

        return bytesRead;
    }

    @Override
    public void close() {
        try {
            in.close();
        } catch (IOException e) {
            throw JayoException.buildJayoException(e);
        }
    }

    @Override
    public String toString() {
        return "reader(" + in + ")";
    }
}
