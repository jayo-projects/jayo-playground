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

import jayo.playground.core.Buffer;
import jayo.playground.core.JayoEOFException;
import jayo.playground.core.RawReader;
import jayo.playground.core.Reader;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.BiFunction;

import static java.lang.System.Logger.Level.TRACE;
import static jayo.playground.core.impl4.Utils.HEX_DIGIT_CHARS;
import static jayo.playground.core.impl4.Utils.checkOffsetAndCount;


public final class RealBuffer4 implements Buffer {
    private static final System.Logger LOGGER = System.getLogger("jayo.Buffer");

    long byteSize = 0L;
    @Nullable
    Segment head = null;
    @Nullable
    Segment tail = null;

    /**
     * Removes the first element of this queue and returns its successor.
     *
     * @return the new head of this queue, or {@code null} if this queue is now empty.
     */
    @Nullable
    Segment removeHead() {
        // queue was empty
        if (head == null) {
            return null;
        }
        // queue had only one item
        if (head == tail) {
            tail = null;
        }
        final var removed = head;
        head = head.next;
        removed.next = null;
        return head;
    }

    @NonNull
    Segment writableTail(final int minimumCapacity) {
        assert minimumCapacity > 0;

        if (tail == null) {
            final var newTail = SegmentPool.take();
            head = newTail;
            tail = newTail;
            return newTail;
        }

        // the current tail has enough room
        if (tail.owner && tail.limit + minimumCapacity <= Segment.SIZE) {
            return tail;
        }

        // Append a new empty segment to fill up.
        final var newTail = SegmentPool.take();
        tail.next = newTail;
        tail = newTail;
        return newTail;
    }

    void addTail(final @NonNull Segment newTail) {
        assert newTail != null;

        if (tail == null) {
            head = newTail;
        } else {
            tail.next = newTail;
        }
        tail = newTail;
    }

    @Override
    public @NonNull Buffer copyTo(final @NonNull Buffer out,
                                  final long offset,
                                  final long byteCount) {
        Objects.requireNonNull(out);
        checkOffsetAndCount(byteSize, offset, byteCount);

        if (byteCount == 0L) {
            return this;
        }

        final var _out = (RealBuffer4) out;
        var _offset = offset;
        _out.byteSize += byteCount;

        // Skip segments that we aren't copying from.
        var segment = head;
        assert segment != null;
        while (_offset >= segment.limit - segment.pos) {
            _offset -= (segment.limit - segment.pos);
            segment = segment.next;
            assert segment != null;
        }

        var remaining = byteCount;
        // Copy from one segment at a time.
        while (remaining > 0L) {
            assert segment != null;
            final var segmentCopy = segment.sharedCopy();
            segmentCopy.pos += (int) _offset;
            segmentCopy.limit = (int) Math.min(segmentCopy.pos + remaining, segmentCopy.limit);
            _out.addTail(segmentCopy);
            remaining -= segmentCopy.limit - segmentCopy.pos;
            _offset = 0L;
            segment = segment.next;
        }
        return this;
    }

    @Override
    public long bytesAvailable() {
        return byteSize;
    }

    @Override
    public boolean exhausted() {
        return byteSize == 0L;
    }

    @Override
    public boolean request(final long byteCount) {
        if (byteCount < 0L) {
            throw new IllegalArgumentException("byteCount < 0L: " + byteCount);
        }
        return byteSize >= byteCount;
    }

    @Override
    public void require(final long byteCount) {
        if (!request(byteCount)) {
            throw new JayoEOFException();
        }
    }

    @Override
    public @NonNull Reader peek() {
        return new RealReader4(new PeekRawReader(this));
    }

    @Override
    public @NonNull String readString() {
        return readString(byteSize, StandardCharsets.UTF_8);
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
        if (byteSize < byteCount) {
            throw new JayoEOFException();
        }
        if (byteCount == 0L) {
            return "";
        }
        final var _byteCount = (int) byteCount;

        final var _head = head;
        assert _head != null;
        if (byteCount > _head.limit - _head.pos) {
            // If the string spans multiple segments, delegate to readByteArray().
            return new String(readByteArray(_byteCount), charset);
        }

        // else all bytes of this future String are in the head segment itself
        final var result = new String(_head.data, _head.pos, _byteCount, charset);
        _head.pos += _byteCount;
        byteSize -= _byteCount;

        if (_head.pos == _head.limit) {
            removeHead();
            SegmentPool.recycle(_head);
        }

        return result;
    }

    private byte @NonNull [] readByteArray(final int byteCount) {
        final var result = new byte[byteCount];
        readTo(result, 0, byteCount);
        return result;
    }

    private void readTo(final byte @NonNull [] writer,
                        final int offset,
                        final int byteCount) {
        var _offset = offset;
        var remaining = byteCount;
        while (remaining > 0) {
            if (LOGGER.isLoggable(TRACE)) {
                LOGGER.log(TRACE, "Buffer#{0} readTo: reading remaining {1} bytes from this{2}",
                        hashCode(), remaining, System.lineSeparator());
            }
            final var bytesRead = readAtMostToPrivate(writer, _offset, remaining);
            if (bytesRead == -1) {
                throw new JayoEOFException("could not write all the requested bytes to byte array, remaining = " +
                        remaining + "/" + byteCount);
            }
            _offset += bytesRead;
            remaining -= bytesRead;
        }
    }

    private int readAtMostToPrivate(final byte @NonNull [] writer,
                                    final int offset,
                                    final int byteCount) {
        final var _head = head;
        assert _head != null;
        final var toRead = Math.min(byteCount, _head.limit - _head.pos);
        System.arraycopy(_head.data, _head.pos, writer, offset, toRead);
        _head.pos += toRead;
        byteSize -= toRead;

        if (_head.pos == _head.limit) {
            removeHead();
            SegmentPool.recycle(_head);
        }

        return toRead;
    }

    @Override
    public void clear() {
        if (LOGGER.isLoggable(TRACE)) {
            LOGGER.log(TRACE, "Buffer#{0} clear: Start clearing all {1} bytes from this{2}",
                    hashCode(), byteSize, System.lineSeparator());
        }
        if (byteSize == 0L) {
            return;
        }

        var segment = head;
        while (segment != null) {
            final var previous = segment;
            segment = removeHead();
            SegmentPool.recycle(previous);
        }

        byteSize = 0L;
        head = null;
        tail = null;
    }

    @Override
    public void skip(final long byteCount) {
        if (byteCount < 0L) {
            throw new IllegalArgumentException("byteCount < 0L: " + byteCount);
        }
        if (LOGGER.isLoggable(TRACE)) {
            LOGGER.log(TRACE, "Buffer#{0} skip: Start skipping {1} bytes from this{2}",
                    hashCode(), byteCount, System.lineSeparator());
        }
        if (byteCount == 0L) {
            return;
        }
        final var toSkip = Math.min(byteCount, byteSize);
        skipInternal(toSkip);
        if (toSkip < byteCount) {
            throw new JayoEOFException("could not skip " + byteCount + " bytes, skipped: " + toSkip);
        }
    }

    void skipInternal(final long byteCount) {
        var remaining = byteCount;
        while (remaining > 0L) {
            final var _head = head;
            assert _head != null;
            final var toSkip = (int) Math.min(remaining, _head.limit - _head.pos);
            _head.pos += toSkip;

            if (_head.pos == _head.limit) {
                removeHead();
                SegmentPool.recycle(_head);
            }

            remaining -= toSkip;
            byteSize -= toSkip;
        }

        if (LOGGER.isLoggable(TRACE)) {
            LOGGER.log(TRACE, "Buffer#{0} : Finished skipping {1} bytes from this {2}",
                    hashCode(), byteCount, System.lineSeparator());
        }
    }

    @Override
    public void write(final @NonNull Buffer source, final long byteCount) {
        // Move bytes from the head of the source buffer to the tail of this buffer in the most possible effective way!
        // This method is the most crucial part of the Jayo concept based on Buffer = a queue of segments.
        //
        // We must do it while balancing two conflicting goals: don't waste CPU and don't waste memory.
        //
        //
        // Don't waste CPU (i.e., don't copy data around).
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
        if (LOGGER.isLoggable(TRACE)) {
            LOGGER.log(TRACE, "Buffer#{0}: Start writing {1} bytes from reader buffer {2} into this buffer{3}",
                    hashCode(), byteCount, source.hashCode(), System.lineSeparator());
        }

        final var src = (RealBuffer4) source;
        var remaining = byteCount;
        while (remaining > 0L) {
            var srcHead = src.head;
            assert srcHead != null;
            // Is a prefix of the source's head segment all that we need to move?
            if (remaining < srcHead.limit - srcHead.pos) {
                if (tail != null && tail.owner &&
                        remaining + tail.limit - ((tail.isShared()) ? 0 : tail.pos) <= Segment.SIZE) {
                    // Our existing segments are sufficient. Move bytes from the source's head to our tail.
                    srcHead.writeTo(tail, (int) remaining);
                    src.byteSize -= remaining;
                    byteSize += remaining;
                    return;
                }
                // We're going to need another segment. Split the source's head segment in two, then move the first
                // of those two to this buffer.
                srcHead = srcHead.splitHead((int) remaining);
            } else {
                src.removeHead();
            }

            // We removed the source's head segment, now we append it to our tail.
            final var movedByteCount = srcHead.limit - srcHead.pos;
            final var newTail = newTailIfNeeded(tail, srcHead);
            if (newTail != null) {
                addTail(newTail);
            }
            remaining -= movedByteCount;
            src.byteSize -= movedByteCount;
            byteSize += movedByteCount;
        }
    }

    /**
     * Call this when the tail and its predecessor may both be less than half full. In this case, we will copy data so
     * that a segment can be recycled.
     */
    private static @Nullable Segment newTailIfNeeded(final @Nullable Segment currentTail,
                                                     final @NonNull Segment newTail) {
        Objects.requireNonNull(newTail);
        if (currentTail == null || !currentTail.owner) {
            return newTail; // Cannot compact: current tail is null or isn't writable.
        }
        final var toWrite = newTail.limit - newTail.pos;
        final var availableInCurrentTail = Segment.SIZE - currentTail.limit
                + ((currentTail.isShared()) ? 0 : currentTail.pos);
        if (toWrite > availableInCurrentTail) {
            return newTail; // Cannot compact: not enough writable space in the current tail.
        }

        newTail.writeTo(currentTail, toWrite);
        SegmentPool.recycle(newTail);
        return null;
    }

    @Override
    public long readAtMostTo(final @NonNull Buffer destination, final long byteCount) {
        Objects.requireNonNull(destination);
        if (byteCount < 0L) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        }
        if (byteSize == 0L) {
            return -1L;
        }
        final var toWrite = Math.min(byteCount, byteSize);
        destination.write(this, toWrite);
        return toWrite;
    }

    @Override
    public @NonNull Buffer write(final @NonNull String string) {
        Objects.requireNonNull(string);

        final var bytes = string.getBytes(StandardCharsets.UTF_8);
        final var length = bytes.length;
        var pos = 0;
        while (pos < length) {
            final var tail = writableTail(1);
            final var toCopy = Math.min(length - pos, Segment.SIZE - tail.limit);
            System.arraycopy(bytes, pos, tail.data, tail.limit, toCopy);
            pos += toCopy;
            tail.limit += toCopy;
        }
        byteSize += length;
        return this;
    }

    @Override
    public long readHexadecimalUnsignedLong() {
        if (byteSize == 0L) {
            throw new JayoEOFException();
        }

        // This value is always built negatively to accommodate Long.MIN_VALUE.
        var value = 0L;
        var seen = 0;
        var done = false;

        var _head = head;
        do {
            assert _head != null;
            var pos = _head.pos;

            while (pos < _head.limit) {
                final int digit;

                final var b = _head.data[pos];
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

                // Detect when the shift overflows.
                if ((value & -0x1000000000000000L) != 0L) {
                    throw new NumberFormatException("Number too large !");
                }

                value = value << 4;
                value = value | (long) digit;
                pos++;
                seen++;
            }

            if (pos == _head.limit) {
                final var previousHead = _head;
                _head = removeHead();
                SegmentPool.recycle(previousHead);
            } else {
                _head.pos = pos;
            }
        } while (!done && _head != null);

        byteSize -= seen;
        return value;
    }

    @Override
    public long transferFrom(final @NonNull RawReader source) {
        Objects.requireNonNull(source);

        var totalBytesRead = 0L;
        while (true) {
            final var readCount = source.readAtMostTo(this, Segment.SIZE);
            if (readCount == -1L) {
                break;
            }
            totalBytesRead += readCount;
        }
        return totalBytesRead;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }

    @Override
    public @NonNull String toString() {
        if (byteSize == 0L) {
            return "Buffer(size=0)";
        }

        final var maxPrintableBytes = 64;
        var toPrint = (int) Math.min(maxPrintableBytes, byteSize);

        final var builder = new StringBuilder(toPrint * 2 + ((byteSize > maxPrintableBytes) ? 1 : 0));

        var segment = head;
        while (true) {
            assert segment != null;
            var pos = segment.pos;
            final var limit = Math.min(segment.limit, pos + toPrint);

            while (pos < limit) {
                final var b = (int) segment.data[pos++];
                toPrint--;
                // @formatter:off
                builder.append(HEX_DIGIT_CHARS[b >> 4 & 0xf])
                       .append(HEX_DIGIT_CHARS[b      & 0xf]);
                // @formatter:on
            }
            if (toPrint == 0) {
                break;
            }
            segment = segment.next;
        }

        if (byteSize > maxPrintableBytes) {
            builder.append('…');
        }

        return "Buffer(size=" + byteSize + " hex=" + builder + ")";
    }

    public byte getByte(final long index) {
        checkOffsetAndCount(byteSize, index, 1L);
        return seek(index, (segment, offset) -> segment.data[(int) (segment.pos + index - offset)]);
    }

    /**
     * Invoke `lambda` with the segment and offset at `startIndex`. Searches from the front or the back
     * depending on what's closer to `startIndex`.
     */
    private <T> T seek(final long startIndex, BiFunction<Segment, Long, T> lambda) {
        if (head == null) {
            return lambda.apply(null, -1L);
        }

        // no more doubly linked segment queue
//        if (isDoublyLinked() && size - startIndex < startIndex) {
//            // We're scanning in the back half of this buffer. Find the segment starting at the back.
//            offset = size;
//            node = tail;
//            while (true) {
//                assert node != null;
//                offset -= (segment.limit - segment.pos);
//                if (offset <= startIndex || node.prev() == null) {
//                    break;
//                }
//                node = node.prev();
//            }
//        } else {

        var segment = head;
        // We're scanning in the front half of this buffer. Find the segment starting at the front.
        var offset = 0L;
        while (segment != null) {
            final var nextOffset = offset + (segment.limit - segment.pos);
            if (nextOffset > startIndex) {
                break;
            }
            segment = segment.next;
            offset = nextOffset;
        }
        return lambda.apply(segment, offset);
        //}
    }
}
