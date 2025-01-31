/*
 * Copyright (c) 2024-present, pull-vert and Jayo contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package jayo.playground.core.impl1;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;

import static java.lang.System.Logger.Level.TRACE;

sealed class SegmentQueue implements AutoCloseable permits ReaderSegmentQueue {
    final static long MAX_BYTE_SIZE = 128 * 1024;

    private static final System.Logger LOGGER = System.getLogger("jayo.SegmentQueue");

    @Nullable Segment head = null;
    @Nullable Segment tail = null;
    private final @NonNull LongAdder size = new LongAdder();

    final @Nullable Segment removeHead(final @NonNull Segment currentHead) {
        assert currentHead.status == Segment.REMOVING;
        return removeHead(currentHead, null);
    }

    // this method must only be called from Buffer.write call
    final @Nullable Segment removeHead(final @NonNull Segment currentHead, final @Nullable Boolean wasSplit) {
        assert currentHead != null;
        assert currentHead.status == Segment.REMOVING || currentHead.status == Segment.TRANSFERRING;

        final var newHead = currentHead.next;
        if (newHead != null) {
            if (wasSplit != null) {
                currentHead.next = null;
            }
        } else {
            // if removed head was also the tail, remove tail as well
            assert tail == currentHead;
            tail = null;
        }
        if (!Boolean.TRUE.equals(wasSplit)) {
            assert head == currentHead;
            head = newHead;
        }
        if (LOGGER.isLoggable(TRACE)) {
            LOGGER.log(TRACE, """
                            SegmentQueue#{0}: removeHead(). currentHead :
                            {1}
                            newHead :
                            {2}{3}""",
                    hashCode(), currentHead, newHead, System.lineSeparator());
        }
        return newHead;
    }

    final <T> T withWritableTail(final int minimumCapacity,
                                 final @NonNull Function<@NonNull Segment, T> writeAction) {
        assert writeAction != null;
        assert minimumCapacity > 0;
        assert minimumCapacity <= Segment.SIZE;

        if (tail == null || !tail.tryWrite()) {
            return withNewWritableSegment(writeAction, null);
        }

        assert tail.status == Segment.WRITING;
        final var previousLimit = tail.limit;
        // current tail has enough room
        if (tail.owner && previousLimit + minimumCapacity <= Segment.SIZE) {
            final var result = writeAction.apply(tail);
            var written = tail.limit - previousLimit;
            incrementSize(written);
            tail.finishWrite();
            return result;
        }

        return withNewWritableSegment(writeAction, tail);
    }

    private <T> T withNewWritableSegment(final @NonNull Function<@NonNull Segment, T> writeAction,
                                         final @Nullable Segment currentTail) {
        // acquire a new empty segment to fill up.
        final var newTail = SegmentPool.take();
        final var result = writeAction.apply(newTail);
        final var written = newTail.limit;
        if (written > 0) {
            if (currentTail != null) {
                assert currentTail.next == null;
                currentTail.next = newTail;
                currentTail.finishWrite();
            } else {
                head = newTail;
            }
            tail = newTail;
            incrementSize(written);
            newTail.finishWrite();
        } else {
            if (currentTail != null) {
                currentTail.finishWrite();
            }
            // We allocated a tail segment, but didn't end up needing it. Recycle!
            SegmentPool.recycle(newTail);
        }
        return result;
    }

    final @Nullable Segment nonRemovedTailOrNull() {
        if (tail == null) {
            if (LOGGER.isLoggable(TRACE)) {
                LOGGER.log(TRACE,
                        "SegmentQueue#{0}: nonRemovedTailOrNull() no current tail, return null{1}",
                        hashCode(), System.lineSeparator());
            }
            return null;
        }

        if (tail.tryWrite()) {
            if (LOGGER.isLoggable(TRACE)) {
                LOGGER.log(TRACE, """
                                SegmentQueue#{0}: nonRemovedTailOrNull() switch to write status and return current non removed tail :
                                {1}{2}""",
                        hashCode(), tail, System.lineSeparator());
            }
            return tail;
        }

        // tail was removed, return null
        if (LOGGER.isLoggable(TRACE)) {
            LOGGER.log(TRACE, """
                            SegmentQueue#{0}: nonRemovedTailOrNull() current tail was removed :
                            {1}
                            , return null{2}""",
                    hashCode(), tail, System.lineSeparator());
        }
        return null;
    }

    final @NonNull Segment addWritableTail(final @Nullable Segment currentTail,
                                           final @NonNull Segment newTail,
                                           final boolean finishWriteInSegments) {
        assert newTail != null;
        if (LOGGER.isLoggable(TRACE)) {
            LOGGER.log(TRACE, """
                            SegmentQueue#{0}: addWritableTail. currentTail :
                            {1}
                            , newTail :
                            {2}{3}""",
                    hashCode(), currentTail, newTail, System.lineSeparator());
        }
        try {
            if (currentTail == null) {
                assert head == null;
                assert tail == null;
                head = newTail;
                tail = newTail;
                return newTail;
            }

            final var previousTail = tail;
            tail = newTail;
            if (previousTail == currentTail) {
                assert currentTail.next == null;
                currentTail.next = newTail;
                if (finishWriteInSegments) {
                    currentTail.finishWrite();
                }
            } else {
                assert head == null;
                head = newTail;
            }

            return newTail;
        } finally {
            if (finishWriteInSegments) {
                newTail.finishWrite();
            }
        }
    }


    long expectSize(final long expectedSize) {
        assert expectedSize > 0L;
        return size();
    }


    long size() {
        return size.longValue();
    }

    final void incrementSize(final long increment) {
        assert increment >= 0;
        if (increment == 0L) {
            return;
        }
        size.add(increment);
    }

    final void decrementSize(final long decrement) {
        assert decrement >= 0;
        if (decrement == 0L) {
            return;
        }
        size.add(-decrement);
    }

    @Override
    public void close() {
        // NOP
    }

    @Override
    public String toString() {
        return "SegmentQueue#" + hashCode() + "{" +
                " size=" + size + System.lineSeparator() +
                ", head=" + head + System.lineSeparator() +
                ", tail=" + tail +
                '}';
    }
}
