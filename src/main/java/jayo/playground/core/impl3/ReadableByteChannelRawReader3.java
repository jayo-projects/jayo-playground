/*
 * Copyright (c) 2024-present, pull-vert and Jayo contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package jayo.playground.core.impl3;

import jayo.playground.core.Buffer;
import jayo.playground.core.JayoException;
import jayo.playground.core.RawReader;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.util.Objects;

import static java.lang.System.Logger.Level.TRACE;

public final class ReadableByteChannelRawReader3 implements RawReader {
    private static final System.Logger LOGGER = System.getLogger("jayo.ReadableByteChannelRawReader");

    private final @NonNull ReadableByteChannel in;

    public ReadableByteChannelRawReader3(final @NonNull ReadableByteChannel in) {
        this.in = Objects.requireNonNull(in);
    }

    /**
     * Execute a single read from the ReadableByteChannel, which reads up to byteCount bytes of data from the readable
     * channel. A smaller number may be read.
     *
     * @return the number of bytes actually read.
     */
    @Override
    public long readAtMostTo(final @NonNull Buffer destination, final long byteCount) {
        Objects.requireNonNull(destination);
        if (byteCount < 0L) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        }
        final var buffer = (RealBuffer3) destination;

        if (byteCount == 0L) {
            return 0L;
        }

        if (LOGGER.isLoggable(TRACE)) {
            LOGGER.log(TRACE, "ReadableByteChannelRawReader: Start reading up to {0} bytes from the " +
                    "ReadableByteChannel to Buffer({2}){3}", byteCount, buffer, System.lineSeparator());
        }

        final var tail = buffer.writableSegment(1);
        final var tailByteBuffer = tail.byteBuffer;
        tailByteBuffer.mark();
        tailByteBuffer.position(tailByteBuffer.limit());
        final var toRead = (int) Math.min(byteCount, Segment.SIZE - tailByteBuffer.limit());
        tailByteBuffer.limit(tailByteBuffer.limit() + toRead);
        try {
            final var read = in.read(tailByteBuffer);
            if (LOGGER.isLoggable(TRACE)) {
                LOGGER.log(TRACE, "ReadableByteChannelRawReader: Finished reading {0}/{1} bytes from the " +
                                "ReadableByteChannel to Buffer({3}){4}",
                        read, byteCount, buffer, System.lineSeparator());
            }
            buffer.byteSize += read;
            return read;
        } catch (IOException e) {
            throw JayoException.buildJayoException(e);
        } finally {
            tailByteBuffer.limit(tailByteBuffer.position());
            tailByteBuffer.reset();
        }
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
