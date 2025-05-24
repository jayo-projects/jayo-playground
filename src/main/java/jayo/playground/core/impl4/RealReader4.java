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

package jayo.playground.core.impl4;

import jayo.playground.core.*;
import org.jspecify.annotations.NonNull;

import java.nio.charset.Charset;
import java.util.Objects;

public final class RealReader4 implements Reader {
    private final @NonNull RawReader reader;
    final @NonNull RealBuffer4 buffer = new RealBuffer4();
    private boolean closed = false;

    public RealReader4(final @NonNull RawReader reader) {
        assert reader != null;
        this.reader = reader;
    }

    @Override
    public long readAtMostTo(final @NonNull Buffer destination, final long byteCount) {
        Objects.requireNonNull(destination);
        if (byteCount < 0L) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        }
        if (closed) {
            throw new JayoClosedResourceException();
        }

        if (buffer.bytesAvailable() == 0L) {
            if (byteCount == 0L) {
                return 0L;
            }
            if (reader.readAtMostTo(buffer, Segment.SIZE) == -1L) {
                return -1L;
            }
        }

        long toRead = Math.min(byteCount, buffer.bytesAvailable());
        return buffer.readAtMostTo(destination, toRead);
    }

    @Override
    public @NonNull String readString() {
        buffer.transferFrom(reader);
        return buffer.readString();
    }

    @Override
    public @NonNull String readString(final long byteCount) {
        if (byteCount < 0 || byteCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("invalid byteCount: " + byteCount);
        }
        require(byteCount);
        return buffer.readString(byteCount);
    }

    @Override
    public @NonNull String readString(final long byteCount, final @NonNull Charset charset) {
        Objects.requireNonNull(charset);
        if (byteCount < 0 || byteCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("invalid byteCount: " + byteCount);
        }
        require(byteCount);
        return buffer.readString(byteCount, charset);
    }

    @Override
    public long readHexadecimalUnsignedLong() {
        require(1L);

        var pos = 0L;
        while (request(pos + 1)) {
            final var b = buffer.getByte(pos);
            if ((b < (byte) ((int) '0') || b > (byte) ((int) '9')) &&
                    (b < (byte) ((int) 'a') || b > (byte) ((int) 'f')) &&
                    (b < (byte) ((int) 'A') || b > (byte) ((int) 'F'))
            ) {
                // Non-digit, or non-leading negative sign.
                if (pos == 0) {
                    throw new NumberFormatException(
                            "Expected leading [0-9a-fA-F] character but was 0x" + Integer.toString(b, 16));
                }
                break;
            }
            pos++;
        }

        return buffer.readHexadecimalUnsignedLong();
    }

    @Override
    public long bytesAvailable() {
        if (closed) {
            throw new JayoClosedResourceException();
        }
        return buffer.bytesAvailable();
    }

    @Override
    public boolean exhausted() {
        if (closed) {
            throw new JayoClosedResourceException();
        }
        return buffer.exhausted() && reader.readAtMostTo(buffer, Segment.SIZE) == -1L;
    }

    @Override
    public boolean request(final long byteCount) {
        if (byteCount < 0L) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        }
        if (closed) {
            throw new JayoClosedResourceException();
        }
        while (buffer.bytesAvailable() < byteCount) {
            if (reader.readAtMostTo(buffer, Segment.SIZE) == -1L) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void require(final long byteCount) {
        if (!request(byteCount)) {
            throw new JayoEOFException("could not read " + byteCount + " bytes from reader, had "
                    + buffer.bytesAvailable());
        }
    }

    @Override
    public void skip(final long byteCount) {
        if (byteCount < 0L) {
            throw new IllegalArgumentException("byteCount < 0L: " + byteCount);
        }
        if (closed) {
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
            if (buffer.bytesAvailable() == 0L && reader.readAtMostTo(buffer, Segment.SIZE) == -1L) {
                throw new JayoEOFException();
            }
            final var toSkip = Math.min(remaining, buffer.bytesAvailable());
            buffer.skipInternal(toSkip);
            remaining -= toSkip;
        }
        return byteCount - remaining;
    }

    @Override
    public @NonNull Reader peek() {
        return new RealReader4(new PeekRawReader(this));
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        reader.close();
        buffer.clear();
    }

    @Override
    public String toString() {
        return "buffered(" + reader + ")";
    }
}
