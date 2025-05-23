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

package jayo.playground.core.impl3;

import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.LongAdder;

final class Segment {
    /**
     * The size of all segments in bytes.
     *
     * @implNote Aligned with TLS max data size = 16_709 bytes
     */
    public static final int SIZE = 16_709;

    /**
     * A segment will be shared if the data size exceeds this threshold, to avoid having to copy this many bytes.
     */
    private static final int SHARE_MINIMUM = 1024; // todo should it be more now that size is 16 KB ?

    /**
     * The binary data.
     */
    final @NonNull ByteBuffer byteBuffer;

    /**
     * Tracks number of shared copies.
     */
    final @NonNull CopyTracker copyTracker;

    Segment() {
        byteBuffer = ByteBuffer.allocate(SIZE);
        byteBuffer.limit(0);
        copyTracker = new CopyTracker(this);
    }

    private Segment(final @NonNull ByteBuffer byteBuffer, final @NonNull CopyTracker copyTracker) {
        assert byteBuffer != null;
        assert copyTracker != null;

        this.byteBuffer = byteBuffer;
        this.copyTracker = copyTracker;
    }

    boolean isOwner() {
        return !byteBuffer.isReadOnly();
    }

    boolean isShared() {
        return copyTracker.copyCount.intValue() > 0;
    }

    /**
     * Returns a new segment that shares the underlying byte array with this one. Adjusting pos and limit is safe, but
     * writes are forbidden. This also marks the current segment as shared, which prevents it from being pooled.
     */
    public @NonNull Segment sharedCopy() {
        // track a new copy created by sharing an associated segment.
        copyTracker.copyCount.increment();

        return new Segment(byteBuffer.asReadOnlyBuffer(), copyTracker);
    }

    /**
     * Splits this segment into two segments. The first segment contains the data in {@code [pos..pos+byteCount)}.
     * The second segment contains the data in {@code [pos+byteCount..limit)}.
     * This is useful when moving partial segments from one buffer to another.
     *
     * @return the new head of the queue.
     */
    @NonNull
    Segment splitHead(final int byteCount) {
        final Segment prefix;

        // We have two competing performance goals:
        //  - Avoid copying data. We achieve this by sharing segments.
        //  - Avoid short shared segments. These are bad for performance because they are readonly and may lead to long
        //    chains of short segments.
        // To balance these goals, we only share segments when the copy will be large.
        if (byteCount >= SHARE_MINIMUM) {
            prefix = sharedCopy();
        } else {
            prefix = SegmentPool.take();
            prefix.byteBuffer.put(0, byteBuffer, byteBuffer.position(), byteCount);
        }
        prefix.byteBuffer.limit(prefix.byteBuffer.position() + byteCount);
        byteBuffer.position(byteBuffer.position() + byteCount);

        return prefix;
    }

    /**
     * Moves {@code byteCount} bytes from this segment to {@code targetSegment}.
     */
    void writeTo(final @NonNull Segment targetSegment, final int byteCount) {
        assert targetSegment != null;

        if (targetSegment.byteBuffer.limit() + byteCount > SIZE) {
            // We can't fit byteCount bytes at the writer's current position. Shift writer first.
            assert targetSegment.isOwner();
            if (targetSegment.byteBuffer.limit() + byteCount - targetSegment.byteBuffer.position() > SIZE) {
                throw new IllegalArgumentException("not enough space in writer segment to write " + byteCount + " bytes");
            }
            targetSegment.byteBuffer.compact();
        }

        targetSegment.byteBuffer.put(targetSegment.byteBuffer.position(), byteBuffer, byteBuffer.position(), byteCount);
        targetSegment.byteBuffer.limit(targetSegment.byteBuffer.limit() + byteCount);
        byteBuffer.position(byteBuffer.position() + byteCount);
    }

    @Override
    public String toString() {
        return "Segment#" + hashCode() + " [capacity=" + byteBuffer.capacity() + "] {" + System.lineSeparator() +
                ", pos=" + byteBuffer.position() +
                ", limit=" + byteBuffer.limit() +
                ", shared=" + isShared() +
                ", owner=" + isOwner() +
                '}';
    }

    /**
     * Reference counting SegmentCopyTracker tracking the number of shared segment copies.
     * Every {@code copyCount.increment()} call increments the counter, every {@link #removeCopy} decrements it.
     * <p>
     * After calling {@link #removeCopy} the same number of times {@code copyCount.increment()} was called, this tracker
     * returns to the unshared state.
     */
    static final class CopyTracker {
        final @NonNull Segment origin;
        private final @NonNull LongAdder copyCount;

        private CopyTracker(final @NonNull Segment origin) {
            assert origin != null;

            this.origin = origin;
            copyCount = new LongAdder();
        }

        /**
         * Records reclamation of a shared segment copy associated with this tracker.
         * If a tracker was in unshared state, this call should not affect an internal state.
         *
         * @return {@code true} if the segment was not shared <i>before</i> this call.
         */
        boolean removeCopy() {
            // The value could not be incremented from `0` under the race, so once it zero, it remains zero in the scope
            // of this call.
            if (copyCount.intValue() == 0) {
                return false;
            }


            copyCount.decrement();
            final var updatedValue = copyCount.intValue();

            // If there are several copies, the last decrement will update copyCount from 0 to -1.
            // That would be the last standing copy, and we can recycle it.
            // If, however, the decremented value falls below -1, it's an error as there were more `removeCopy` than
            // `addCopy` calls.
            if (updatedValue >= 0) {
                return true;
            }
            if (updatedValue < -1) {
                throw new IllegalStateException("Shared copies count is negative: " + updatedValue + 1);
            }
            copyCount.reset();
            return false;
        }
    }
}
