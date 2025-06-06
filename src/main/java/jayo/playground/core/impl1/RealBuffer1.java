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
import jayo.playground.core.JayoEOFException;
import jayo.playground.core.Reader;
import jayo.playground.core.Wrapper;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static java.lang.System.Logger.Level.TRACE;
import static jayo.playground.core.impl1.Segment.TRANSFERRING;
import static jayo.playground.core.impl1.Segment.WRITING;
import static jayo.playground.core.impl1.Utils.HEX_DIGIT_CHARS;
import static jayo.playground.core.impl1.Utils.checkOffsetAndCount;


public final class RealBuffer1 implements Buffer {
    private static final System.Logger LOGGER = System.getLogger("jayo.Buffer");

    final @NonNull SegmentQueue segmentQueue;

    public RealBuffer1() {
        this(new SegmentQueue());
    }

    RealBuffer1(final @NonNull SegmentQueue segmentQueue) {
        this.segmentQueue = Objects.requireNonNull(segmentQueue);
    }

    @Override
    public @NonNull Buffer copyTo(final @NonNull Buffer out,
                                  final long offset,
                                  final long byteCount) {
        Objects.requireNonNull(out);
        checkOffsetAndCount(bytesAvailable(), offset, byteCount);
        if (!(out instanceof RealBuffer1 _out)) {
            throw new IllegalArgumentException("out must be an instance of RealBuffer");
        }
        if (byteCount == 0L) {
            return this;
        }

        var _offset = offset;
        var _byteCount = byteCount;

        // Skip segment nodes that we aren't copying from.
        var segment = segmentQueue.head;
        assert segment != null;
        var segmentSize = segment.limit - segment.pos;
        while (_offset >= segmentSize) {
            _offset -= segmentSize;
            segment = segment.next;
            assert segment != null;
            segmentSize = segment.limit - segment.pos;
        }

        // Copy from one segment at a time.
        while (_byteCount > 0L) {
            assert segment != null;
            final var segmentCopy = segment.sharedCopy();
            final var pos = (int) (segmentCopy.pos + _offset);
            segmentCopy.pos = pos;
            final var limit = Math.min(pos + (int) _byteCount, segmentCopy.limit);
            segmentCopy.limit = limit;
            final var written = limit - pos;
            final var outTail = _out.segmentQueue.nonRemovedTailOrNull();
            _out.segmentQueue.addWritableTail(outTail, segmentCopy, true);
            _out.segmentQueue.incrementSize(written);
            _byteCount -= written;
            _offset = 0L;
            segment = segment.next;
        }
        return this;
    }

    @Override
    public long bytesAvailable() {
        return segmentQueue.size();
    }

    @Override
    public @NonNull Reader peek() {
        return new RealReader1(new PeekRawReader(this));
    }

    @Override
    public boolean exhausted() {
        return segmentQueue.size() == 0L;
    }

    @Override
    public boolean request(final long byteCount) {
        if (byteCount < 0L) {
            throw new IllegalArgumentException("byteCount < 0L: " + byteCount);
        }
        return segmentQueue.size() >= byteCount;
    }

    @Override
    public void require(final long byteCount) {
        if (!request(byteCount)) {
            throw new JayoEOFException();
        }
    }

    @Override
    public @NonNull String readString() {
        return readString(segmentQueue.size(), StandardCharsets.UTF_8);
    }

    @Override
    public @NonNull String readString(final long byteCount) {
        return readString(byteCount, StandardCharsets.UTF_8);
    }

    @Override
    public @NonNull String readString(final long byteCount, final @NonNull Charset charset) {
        Objects.requireNonNull(charset);
        if (byteCount < 0 || byteCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("invalid byteCount: " + byteCount);
        }
        if (segmentQueue.size() < byteCount) {
            throw new JayoEOFException();
        }
        if (byteCount == 0L) {
            return "";
        }

        final var head = segmentQueue.head;
        assert head != null;
        final var currentLimit = head.limit;
        if (head.pos + byteCount > currentLimit) {
            // If the string spans multiple segments, delegate to readByteArray().
            return new String(readByteArray(head, (int) byteCount), charset);
        }

        // else all bytes of this future String are in head Segment itself
        final var result = new String(head.data, head.pos, (int) byteCount, charset);
        head.pos += (int) byteCount;
        segmentQueue.decrementSize(byteCount);

        if (head.pos == currentLimit && head.tryRemove() && head.validateRemove()) {
            segmentQueue.removeHead(head);
            SegmentPool.recycle(head);
        }

        return result;
    }

    private byte @NonNull [] readByteArray(final @NonNull Segment head, final int byteCount) {
        final var result = new byte[byteCount];
        readTo(head, result, 0, byteCount);
        return result;
    }

    private void readTo(final @NonNull Segment head,
                        final byte @NonNull [] writer,
                        final int offset,
                        final int byteCount) {
        var _head = head;
        var _offset = offset;
        final var toWrite = (int) Math.min(byteCount, segmentQueue.size());
        var remaining = toWrite;
        var finished = false;
        while (!finished) {
            assert _head != null;
            final var currentLimit = _head.limit;
            final var toCopy = Math.min(remaining, currentLimit - _head.pos);
            remaining -= toCopy;
            finished = remaining == 0;
            _head = readAtMostTo(_head, writer, _offset, toCopy, currentLimit);
            _offset += toCopy;
        }

        if (toWrite < byteCount) {
            throw new JayoEOFException("could not write all the requested bytes to byte array, written " +
                    toWrite + "/" + byteCount);
        }
    }

    private @Nullable Segment readAtMostTo(final @NonNull Segment head,
                                           final byte @NonNull [] writer,
                                           final int offset,
                                           final int byteCount,
                                           final int currentLimit) {
        final var toCopy = Math.min(byteCount, currentLimit - head.pos);
        System.arraycopy(head.data, head.pos, writer, offset, toCopy);
        head.pos += toCopy;
        segmentQueue.decrementSize(toCopy);

        if (head.pos == currentLimit && head.tryRemove() && head.validateRemove()) {
            final var nextHead = segmentQueue.removeHead(head);
            SegmentPool.recycle(head);
            return nextHead;
        }

        return null;
    }

    @Override
    public void clear() {
        final var size = segmentQueue.expectSize(Long.MAX_VALUE);
        if (LOGGER.isLoggable(TRACE)) {
            LOGGER.log(TRACE, """
                            Buffer(SegmentQueue#{0}) clear(): Start clearing all {1} bytes from this
                            {2}{3}""",
                    segmentQueue.hashCode(), size, segmentQueue, System.lineSeparator());
        }
        skipInternal(size);
    }

    @Override
    public void skip(final long byteCount) {
        if (byteCount < 0L) {
            throw new IllegalArgumentException("byteCount < 0L: " + byteCount);
        }
        if (byteCount == 0L) {
            return;
        }
        final var toSkip = Math.min(byteCount, segmentQueue.expectSize(byteCount));
        skipInternal(toSkip);
        if (toSkip < byteCount) {
            throw new JayoEOFException("could not skip " + byteCount + " bytes, skipped: " + toSkip);
        }
    }

    void skipInternal(final long byteCount) {
        if (byteCount == 0L) {
            return;
        }
        if (LOGGER.isLoggable(TRACE)) {
            LOGGER.log(TRACE, """
                            Buffer(SegmentQueue#{0}) : Start skipping {1} bytes from this
                            {2}{3}""",
                    segmentQueue.hashCode(), byteCount, segmentQueue, System.lineSeparator());
        }
        var remaining = byteCount;
        var head = segmentQueue.head;
        assert head != null;
        var headLimit = head.limit;
        while (remaining > 0L) {
            if (head.pos == headLimit) {
                if (!head.tryRemove()) {
                    throw new IllegalStateException("Non tail segment must be removable");
                }
                final var oldHead = head;
                head = segmentQueue.removeHead(head);
                assert head != null;
                headLimit = head.limit;
                SegmentPool.recycle(oldHead);
            }

            var toSkipInSegment = (int) Math.min(remaining, headLimit - head.pos);
            head.pos += toSkipInSegment;
            segmentQueue.decrementSize(toSkipInSegment);
            remaining -= toSkipInSegment;
        }
        if (head.pos == head.limit && head.tryRemove()) {
            segmentQueue.removeHead(head);
            SegmentPool.recycle(head);
        }

        if (LOGGER.isLoggable(TRACE)) {
            LOGGER.log(TRACE, """
                            Buffer(SegmentQueue#{0}) : Finished skipping {1} bytes from this
                            {2}{3}""",
                    segmentQueue.hashCode(), byteCount, segmentQueue, System.lineSeparator());
        }
    }

    @Override
    public void write(final @NonNull Buffer source, final long byteCount) {
        // Move bytes from the head of the reader buffer to the tail of this buffer in the most possible effective way !
        // This method is the most crucial part of the Jayo concept based on Buffer = a queue of segments.
        //
        // We must do it while balancing two conflicting goals: don't waste CPU and don't waste memory.
        //
        //
        // Don't waste CPU (i.e. don't copy data around).
        //
        // Copying large amounts of data is expensive. Instead, we prefer to reassign entire segments from one buffer to
        // the other.
        //
        //
        // Don't waste memory.
        //
        // As an invariant, adjacent pairs of segments in a buffer should be at least 50% full, except for the head
        // segment and the tail segment.
        //
        // The head segment cannot maintain the invariant because the application is consuming bytes from this segment,
        // decreasing its level.
        //
        // The tail segment cannot maintain the invariant because the application is producing bytes, which may require
        // new nearly-empty tail segments to be appended.
        //
        //
        // Moving segments between buffers
        //
        // When writing one buffer to another, we prefer to reassign entire segments over copying bytes into their most
        // compact form. Suppose we have a buffer with these segment levels [91%, 61%]. If we append a buffer with a
        // single [72%] segment, that yields [91%, 61%, 72%]. No bytes are copied.
        //
        // Or suppose we have a buffer with these segment levels: [100%, 2%], and we want to append it to a buffer with
        // these segment levels [99%, 3%]. This operation will yield the following segments: [100%, 2%, 99%, 3%]. That
        // is, we do not spend time copying bytes around to achieve more efficient memory use like [100%, 100%, 4%].
        //
        // When combining buffers, we will compact adjacent buffers when their combined level doesn't exceed 100%. For
        // example, when we start with [100%, 40%] and append [30%, 80%], the result is [100%, 70%, 80%].
        //
        //
        // Splitting segments
        //
        // Occasionally we write only part of a reader buffer to a writer buffer. For example, given a writer [51%, 91%], we
        // may want to write the first 30% of a reader [92%, 82%] to it. To simplify, we first transform the reader to
        // an equivalent buffer [30%, 62%, 82%] and then move the head segment, yielding writer [51%, 91%, 30%] and reader
        // [62%, 82%].

        if (Objects.requireNonNull(source) == this) {
            throw new IllegalArgumentException("reader == this, cannot write in itself");
        }
        checkOffsetAndCount(source.bytesAvailable(), 0, byteCount);
        if (byteCount == 0L) {
            return;
        }
        if (!(source instanceof RealBuffer1 _reader)) {
            throw new IllegalArgumentException("reader must be an instance of RealBuffer");
        }

        if (LOGGER.isLoggable(TRACE)) {
            LOGGER.log(TRACE, """
                            Buffer(SegmentQueue#{0}) : Start writing {1} bytes from reader segment queue
                            {2}
                            into this segment queue
                            {3}{4}""",
                    segmentQueue.hashCode(), byteCount, _reader.segmentQueue, segmentQueue, System.lineSeparator());
        }

        var remaining = byteCount;
        var tail = segmentQueue.nonRemovedTailOrNull();
        var readerHead = _reader.segmentQueue.head;
        Segment nextReaderHead = null;
        try {
            while (remaining > 0) {
                if (nextReaderHead != null) {
                    readerHead = nextReaderHead;
                }
                assert readerHead != null;

                // Is a prefix of the reader's head segment all that we need to move?
                var currentLimit = readerHead.limit;
                var bytesInReader = currentLimit - readerHead.pos;
                var split = false;
                if (remaining < bytesInReader) {
                    if (tryTransferBytes(tail, readerHead, _reader, remaining)) {
                        return;
                    }
                    split = true;
                }

                if (!split) {
                    split = readerHead.startTransfer();
                    if (!split) {
                        // check after switching to Transferring state
                        final var newLimit = readerHead.limit;
                        if (newLimit != currentLimit) {
                            bytesInReader = newLimit - readerHead.pos;
                            if (remaining < bytesInReader) {
                                if (tryTransferBytes(tail, readerHead, _reader, remaining)) {
                                    return;
                                }
                                split = true;
                            } else {
                                currentLimit = newLimit;
                            }
                        }
                    }
                    if (LOGGER.isLoggable(TRACE)) {
                        LOGGER.log(TRACE,
                                "reader SegmentQueue#{0} : head Segment#{1} is writing = {2}{3}",
                                _reader.segmentQueue.hashCode(), readerHead.hashCode(), split,
                                System.lineSeparator());
                    }
                }

                if (split) {
                    // We're going to need another segment. Split the reader's head segment in two, then we will move
                    // the first of those two to this buffer.
                    nextReaderHead = readerHead;

                    bytesInReader = (int) Math.min(bytesInReader, remaining);
                    readerHead = readerHead.splitHead(bytesInReader);
                    if (LOGGER.isLoggable(TRACE)) {
                        LOGGER.log(TRACE,
                                "reader SegmentQueue#{0} : splitHead. prefix Segment#{1}, suffix Segment#{2}{3}",
                                _reader.segmentQueue.hashCode(), readerHead.hashCode(), nextReaderHead.hashCode(),
                                System.lineSeparator());
                    }
                    currentLimit = readerHead.limit;
                }

                assert readerHead.status == TRANSFERRING;

                // Remove the reader's head segment and append it to our tail.
                final var movedByteCount = currentLimit - readerHead.pos;

                _reader.segmentQueue.decrementSize(movedByteCount);
                nextReaderHead = _reader.segmentQueue.removeHead(readerHead, split);

                if (LOGGER.isLoggable(TRACE)) {
                    LOGGER.log(TRACE,
                            "Buffer(SegmentQueue#{0}) : decrement {1} bytes of reader SegmentQueue#{2}{3}",
                            segmentQueue.hashCode(), movedByteCount, _reader.segmentQueue.hashCode(),
                            System.lineSeparator());
                }

                final var newTail = newTailIfNeeded(tail, readerHead);
                // newTail != null is true if we will transfer readerHead to our buffer
                if (newTail != null) {
                    segmentQueue.addWritableTail(tail, newTail, false);

                    // transfer is finished
                    assert newTail.status == TRANSFERRING;
                    newTail.status = WRITING;

                    if (LOGGER.isLoggable(TRACE)) {
                        LOGGER.log(TRACE,
                                "Buffer(SegmentQueue#{0}) : transferred Segment#{1} of {2} bytes from reader SegmentQueue#{3}{4}",
                                segmentQueue.hashCode(), newTail.hashCode(), movedByteCount, _reader.segmentQueue.hashCode(), System.lineSeparator());
                    }

                    segmentQueue.incrementSize(movedByteCount);
                    if (tail != null) {
                        tail.finishWrite();
                    }
                    tail = newTail;
                } else {
                    segmentQueue.incrementSize(movedByteCount);
                    readerHead = null;
                }

                if (LOGGER.isLoggable(TRACE)) {
                    LOGGER.log(TRACE,
                            "Buffer(SegmentQueue#{0}) : incremented {1} bytes of this segment queue{2}{3}",
                            segmentQueue.hashCode(), movedByteCount, segmentQueue, System.lineSeparator());
                }

                remaining -= movedByteCount;
            }
        } finally {
            if (tail != null) {
                tail.finishWrite();
            }
        }
        if (LOGGER.isLoggable(TRACE)) {
            LOGGER.log(TRACE, """
                            Buffer(SegmentQueue#{0}) : Finished writing {1} bytes from reader segment queue
                            {2}
                            into this segment queue
                            {3}{4}""",
                    segmentQueue.hashCode(), byteCount, _reader.segmentQueue, segmentQueue, System.lineSeparator());
        }
    }

    private boolean tryTransferBytes(Segment tail, Segment readerHead, RealBuffer1 reader, long byteCount) {
        if (tail != null && tail.owner &&
                byteCount + tail.limit - ((tail.isShared()) ? 0 : tail.pos) <= Segment.SIZE) {
            try {
                // Our existing segments are sufficient. Transfer bytes from reader's head to our tail.
                readerHead.writeTo(tail, (int) byteCount);
                if (LOGGER.isLoggable(TRACE)) {
                    LOGGER.log(TRACE, "Buffer(SegmentQueue#{0}) : transferred {1} bytes from reader " +
                                    "Segment#{2} to target Segment#{3}{4}",
                            segmentQueue.hashCode(), byteCount, readerHead.hashCode(), tail.hashCode(),
                            System.lineSeparator());
                }
                reader.segmentQueue.decrementSize(byteCount);
                segmentQueue.incrementSize(byteCount);
                return true;
            } finally {
                readerHead.finishTransfer();
            }
        }
        return false;
    }

    /**
     * Call this when the tail and its predecessor may both be less than half full. In this case, we will copy data so
     * that a segment can be recycled.
     */
    private @Nullable Segment newTailIfNeeded(final @Nullable Segment currentTail,
                                              final @NonNull Segment newTail) {
        Objects.requireNonNull(newTail);
        if (currentTail == null || !currentTail.owner) {
            // Cannot compact: current tail is null or isn't writable.
            return newTail;
        }
        final var byteCount = newTail.limit - newTail.pos;
        final var availableByteCount = Segment.SIZE - currentTail.limit
                + ((currentTail.isShared()) ? 0 : currentTail.pos);
        if (byteCount > availableByteCount) {
            // Cannot compact: not enough writable space in current tail.
            return newTail;
        }

        newTail.writeTo(currentTail, byteCount);
        if (LOGGER.isLoggable(TRACE)) {
            LOGGER.log(TRACE, """
                            Buffer(SegmentQueue#{0}) : tail and its predecessor were both less than half full,
                            transferred {1} bytes from reader segment
                            {2}
                            to target segment
                            {3}{4}""",
                    segmentQueue.hashCode(), byteCount, newTail, currentTail, System.lineSeparator());
        }
        SegmentPool.recycle(newTail);
        return null;
    }

    @Override
    public long readAtMostTo(final @NonNull Buffer destination, final long byteCount) {
        Objects.requireNonNull(destination);
        if (byteCount < 0L) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        }
        final var size = segmentQueue.size();
        if (size == 0L) {
            return -1L;
        }
        var _byteCount = Math.min(byteCount, size);
        destination.write(this, _byteCount);
        return _byteCount;
    }

    @Override
    public @NonNull Buffer write(final @NonNull String string) {
        Objects.requireNonNull(string);

        final var bytes = string.getBytes(StandardCharsets.UTF_8);
        final var length = bytes.length;
        final var pos = new Wrapper.Int(0);
        while (pos.value < length) {
            segmentQueue.withWritableTail(1, tail -> {
                final var toCopy = Math.min(length - pos.value, Segment.SIZE - tail.limit);
                System.arraycopy(bytes, pos.value, tail.data, tail.limit, toCopy);
                pos.value += toCopy;
                tail.limit += toCopy;
                return null;
            });
        }
        return this;
    }

    @Override
    public long readHexadecimalUnsignedLong() {
        final var currentSize = segmentQueue.size();
        if (currentSize == 0L) {
            throw new JayoEOFException();
        }

        // This value is always built negatively in order to accommodate Long.MIN_VALUE.
        var value = 0L;
        var seen = 0;
        var done = false;

        while (!done) {
            if (seen >= currentSize && segmentQueue.expectSize(17L) == 0L) {
                break;
            }
            final var head = segmentQueue.head;
            assert head != null;
            final var data = head.data;
            var pos = head.pos;
            final var currentLimit = head.limit;

            while (pos < currentLimit) {
                final int digit;

                final var b = data[pos];
                if (b >= (byte) ((int) '0') && b <= (byte) ((int) '9')) {
                    digit = b - (byte) ((int) '0');
                } else if (b >= (byte) ((int) 'a') && b <= (byte) ((int) 'f')) {
                    digit = b - (byte) ((int) 'a') + 10;
                } else if (b >= (byte) ((int) 'A') && b <= (byte) ((int) 'F')) {
                    digit = b - (byte) ((int) 'A') + 10; // We never write uppercase, but we support reading it.
                } else {
                    if (seen == 0) {
                        throw new NumberFormatException(
                                "Expected leading [0-9a-fA-F] character but was 0x...");
                    }
                    // Set a flag to stop iteration. We still need to run through segment updating below.
                    done = true;
                    break;
                }

                // Detect when the shift will overflow.
                if ((value & -0x1000000000000000L) != 0L) {
                    throw new NumberFormatException("Number too large !");
                }

                value = value << 4;
                value = value | (long) digit;
                pos++;
                seen++;
            }

            final var read = pos - head.pos;
            head.pos = pos;
            segmentQueue.decrementSize(read);

            if (pos == currentLimit && head.tryRemove() && head.validateRemove()) {
                segmentQueue.removeHead(head);
                SegmentPool.recycle(head);
            }
        }

        return value;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
        segmentQueue.close();
    }

    @Override
    public @NonNull String toString() {
        final var currentSize = segmentQueue.size();
        if (currentSize == 0L) {
            return "Buffer(size=0)";
        }

        final var maxPrintableBytes = 64;
        final var len = (int) Math.min(maxPrintableBytes, currentSize);

        final var builder = new StringBuilder(len * 2 + ((currentSize > maxPrintableBytes) ? 1 : 0));

        var segment = segmentQueue.head;
        assert segment != null;
        var written = 0;
        var pos = segment.pos;
        while (written < len) {
            if (pos == segment.limit) {
                segment = segment.next;
                assert segment != null;
                pos = segment.pos;
            }

            final var b = (int) segment.data[pos++];
            written++;

            builder.append(HEX_DIGIT_CHARS[(b >> 4) & 0xf])
                    .append(HEX_DIGIT_CHARS[b & 0xf]);
        }

        if (currentSize > maxPrintableBytes) {
            builder.append('…');
        }

        return "Buffer(size=" + currentSize + " hex=" + builder + ")";
    }
}
