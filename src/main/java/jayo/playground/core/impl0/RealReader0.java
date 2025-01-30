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

import jayo.playground.core.*;
import org.jspecify.annotations.NonNull;

import java.nio.charset.Charset;
import java.util.Objects;

import static jayo.playground.core.impl0.ReaderSegmentQueue.newReaderSegmentQueue;
import static jayo.playground.core.impl0.ReaderSegmentQueue.newSyncReaderSegmentQueue;

public final class RealReader0 implements Reader {
    private static final long INTEGER_MAX_PLUS_1 = (long) Integer.MAX_VALUE + 1;

    final @NonNull ReaderSegmentQueue segmentQueue;

    RealReader0(final @NonNull RawReader reader) {
        Objects.requireNonNull(reader);
        segmentQueue = newSyncReaderSegmentQueue(reader);
    }

    public RealReader0(final @NonNull RawReader reader, final boolean preferAsync) {
        Objects.requireNonNull(reader);
        segmentQueue = newReaderSegmentQueue(reader, preferAsync);
    }

    @Override
    public long readAtMostTo(final @NonNull Buffer writer, final long byteCount) {
        Objects.requireNonNull(writer);
        if (byteCount < 0L) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        }
        if (segmentQueue.closed) {
            throw new JayoClosedResourceException();
        }

        if (segmentQueue.expectSize(1L) == 0L) {
            return -1;
        }

        return segmentQueue.buffer.readAtMostTo(writer, byteCount);
    }

    @Override
    public @NonNull String readString() {
        if (segmentQueue.closed) {
            throw new JayoClosedResourceException();
        }
        segmentQueue.expectSize(INTEGER_MAX_PLUS_1);
        return segmentQueue.buffer.readString();
    }

    @Override
    public @NonNull String readString(final long byteCount) {
        if (byteCount < 0 || byteCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("invalid byteCount: " + byteCount);
        }
        require(byteCount);
        return segmentQueue.buffer.readString(byteCount);
    }

    @Override
    public @NonNull String readString(final long byteCount, final @NonNull Charset charset) {
        Objects.requireNonNull(charset);
        if (byteCount < 0 || byteCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("invalid byteCount: " + byteCount);
        }
        require(byteCount);
        return segmentQueue.buffer.readString(byteCount, charset);
    }

    @Override
    public long bytesAvailable() {
        if (segmentQueue.closed) {
            throw new JayoClosedResourceException();
        }
        return segmentQueue.size();
    }

    @Override
    public boolean exhausted() {
        if (segmentQueue.closed) {
            throw new JayoClosedResourceException();
        }
        return segmentQueue.expectSize(1L) == 0L;
    }

    @Override
    public boolean request(final long byteCount) {
        if (byteCount < 0L) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        }
        if (segmentQueue.closed) {
            throw new JayoClosedResourceException();
        }
        if (byteCount == 0) {
            return true;
        }
        return segmentQueue.expectSize(byteCount) >= byteCount;
    }

    @Override
    public void require(final long byteCount) {
        if (!request(byteCount)) {
            throw new JayoEOFException("could not read " + byteCount + " bytes from reader, had " + segmentQueue.size());
        }
    }

    @Override
    public void skip(final long byteCount) {
        if (byteCount < 0L) {
            throw new IllegalArgumentException("byteCount < 0L: " + byteCount);
        }
        if (segmentQueue.closed) {
            throw new JayoClosedResourceException();
        }
        final var skipped = skipPrivate(byteCount);
        if (skipped < byteCount) {
            throw new JayoEOFException("could not skip " + byteCount + " bytes, skipped: " + skipped);
        }
    }

    private long skipPrivate(final long byteCount) {
        var remaining = byteCount;
        while (remaining > 0) {
            // trying to read SEGMENT_SIZE, so we have at least one complete segment in the buffer
            final var size = segmentQueue.expectSize(remaining);
            if (size == 0L) {
                break;
            }
            final var toSkip = Math.min(remaining, size);
            segmentQueue.buffer.skipInternal(toSkip);
            remaining -= toSkip;
        }
        return byteCount - remaining;
    }

    @Override
    public @NonNull Reader peek() {
        return new RealReader0(new PeekRawReader(this));
    }

    @Override
    public void close() {
        segmentQueue.close();
    }

    @Override
    public String toString() {
        return "buffered(" + segmentQueue.reader + ")";
    }
}
