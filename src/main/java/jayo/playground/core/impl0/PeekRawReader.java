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

package jayo.playground.core.impl0;

import jayo.playground.core.Buffer;
import jayo.playground.core.RawReader;
import jayo.playground.core.Reader;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

import static jayo.playground.core.impl0.Utils.getBufferFromReader;

/**
 * A {@link RawReader} which peeks into an upstream {@link Reader} and allows reading and expanding of the buffered data
 * without consuming it. Does this by requesting additional data from the upstream reader if needed and copying out of
 * the internal buffer of the upstream reader if possible.
 * <p>
 * This reader also maintains a snapshot of the starting location of the upstream buffer which it validates against on
 * every read. If the upstream buffer is read from, this reader will become invalid and throw
 * {@link IllegalStateException} on any future reads.
 */
final class PeekRawReader implements RawReader {
    private final @NonNull Reader upstream;
    private final @NonNull RealBuffer0 buffer;
    private @Nullable Segment expectedSegment;
    private int expectedPos;
    private boolean closed = false;
    private  long pos = 0L;

    public PeekRawReader(final @NonNull Reader upstream) {
        this.upstream = Objects.requireNonNull(upstream);
        buffer = (RealBuffer0) getBufferFromReader(upstream);
        buffer.segmentQueue.expectSize(1L);
        final var bufferHead = buffer.segmentQueue.head();
        if (bufferHead != null) {
            this.expectedSegment = bufferHead;
            this.expectedPos = bufferHead.pos;
        } else {
            this.expectedSegment = null;
            this.expectedPos = -1;
        }
    }

    @Override
    public long readAtMostTo(final @NonNull Buffer destination, final  long byteCount) {
        Objects.requireNonNull(destination);
        if (byteCount < 0L) {
            throw new IllegalArgumentException("byteCount < 0 : " + byteCount);
        }
        if (closed) {
            throw new IllegalStateException("this peek reader is closed");
        }

        final var bufferHead = buffer.segmentQueue.head();
        // Reader becomes invalid if there is an expected Segment and it and the expected position does not match the
        // current head and head position of the upstream buffer
        if (expectedSegment != null &&
                (bufferHead == null
                        || expectedSegment != bufferHead
                        || expectedPos != bufferHead.pos)) {
            throw new IllegalStateException("Peek reader is invalid because upstream reader was used");
        }
        if (byteCount == 0L) {
            return 0L;
        }
        if (!upstream.request(pos + 1)) {
            return -1L;
        }

        if (expectedSegment == null && bufferHead != null) {
            // Only once the buffer actually holds data should an expected Segment and position be recorded.
            // This allows reads from the peek reader to repeatedly return -1 and for data to be added later.
            // Unit tests depend on this behavior.
            expectedSegment = bufferHead;
            expectedPos = bufferHead.pos;
        }

        final var toCopy = Math.min(byteCount, buffer.bytesAvailable() - pos);
        if ((pos | toCopy) < 0) {
            throw new IllegalStateException("Peek reader is invalid because upstream reader was used");
        }
        buffer.copyTo(destination, pos, toCopy);
        pos += toCopy;
        return toCopy;
    }

    @Override
    public void close() {
        closed = true;
    }
}
