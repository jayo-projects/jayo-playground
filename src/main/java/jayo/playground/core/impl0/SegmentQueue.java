/*
 * Copyright (c) 2024-present, pull-vert and Jayo contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package jayo.playground.core.impl0;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;

import static java.lang.System.Logger.Level.TRACE;

sealed class SegmentQueue implements AutoCloseable permits ReaderSegmentQueue {
    final static long MAX_BYTE_SIZE = 128 * 1024;

    private static final System.Logger LOGGER = System.getLogger("jayo.SegmentQueue");

    @SuppressWarnings("FieldMayBeFinal")
    private volatile @Nullable Segment head = null;
    @SuppressWarnings("FieldMayBeFinal")
    private volatile @Nullable Segment tail = null;
    private final @NonNull LongAdder size = new LongAdder();

    // VarHandle mechanics
    static final VarHandle HEAD;
    static final VarHandle TAIL;

    static {
        try {
            final var l = MethodHandles.lookup();
            HEAD = l.findVarHandle(SegmentQueue.class, "head", Segment.class);
            TAIL = l.findVarHandle(SegmentQueue.class, "tail", Segment.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    final @Nullable Segment head() {
        return (Segment) HEAD.getVolatile(this);
    }

    final @Nullable Segment tail() {
        return (Segment) TAIL.getVolatile(this);
    }

    final @Nullable Segment removeHead(final @NonNull Segment currentHead) {
        return removeHead(currentHead, null);
    }

    // this method must only be called from Buffer.write call
    final @Nullable Segment removeHead(final @NonNull Segment currentHead, final @Nullable Boolean wasSplit) {
        assert currentHead != null;
        assert (byte) Segment.STATUS.get(currentHead) == Segment.REMOVING
                || (byte) Segment.STATUS.get(currentHead) == Segment.TRANSFERRING;

        final var newHead = currentHead.nextVolatile();
        if (newHead != null) {
            if (wasSplit != null) {
                Segment.NEXT.compareAndSet(currentHead, newHead, null);
            }
        } else {
            // if removed head was also the tail, remove tail as well
            TAIL.compareAndSet(this, currentHead, null);
        }
        if (!Boolean.TRUE.equals(wasSplit)) {
            HEAD.compareAndSet(this, currentHead, newHead);
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

        final var currentTail = tail();
        if (currentTail == null || !currentTail.tryWrite()) {
            return withNewWritableSegment(writeAction, null);
        }

        assert (byte) Segment.STATUS.get(currentTail) == Segment.WRITING;
        final var previousLimit = currentTail.limit();
        // current tail has enough room
        if (currentTail.owner && previousLimit + minimumCapacity <= Segment.SIZE) {
            var written = 0;
            try {
                final var result = writeAction.apply(currentTail);
                written = currentTail.limit() - previousLimit;
                return result;
            } finally {
                incrementSize(written);
                currentTail.finishWrite();
            }
        }

        return withNewWritableSegment(writeAction, currentTail);
    }

    private <T> T withNewWritableSegment(final @NonNull Function<@NonNull Segment, T> writeAction,
                                         final @Nullable Segment currentTail) {
        // acquire a new empty segment to fill up.
        final var newTail = SegmentPool.take();
        final var result = writeAction.apply(newTail);
        final var written = newTail.limit();
        if (written > 0) {
            try {
                if (currentTail != null) {
                    if (!Segment.NEXT.compareAndSet(currentTail, null, newTail)) {
                        throw new IllegalStateException("Could not add new Segment after current tail, " +
                                "next node should be null");
                    }
                    currentTail.finishWrite();
                } else {
                    HEAD.setVolatile(this, newTail);
                }
                TAIL.setVolatile(this, newTail);
            } finally {
                incrementSize(written);
                newTail.finishWrite();
            }
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
        final var currentTail = tail();
        if (currentTail == null) {
            if (LOGGER.isLoggable(TRACE)) {
                LOGGER.log(TRACE,
                        "SegmentQueue#{0}: nonRemovedTailOrNull() no current tail, return null{1}",
                        hashCode(), System.lineSeparator());
            }
            return null;
        }

        if (currentTail.tryWrite()) {
            if (LOGGER.isLoggable(TRACE)) {
                LOGGER.log(TRACE, """
                                SegmentQueue#{0}: nonRemovedTailOrNull() switch to write status and return current non removed tail :
                                {1}{2}""",
                        hashCode(), currentTail, System.lineSeparator());
            }
            return currentTail;
        }

        // tail was removed, return null
        if (LOGGER.isLoggable(TRACE)) {
            LOGGER.log(TRACE, """
                            SegmentQueue#{0}: nonRemovedTailOrNull() current tail was removed :
                            {1}
                            , return null{2}""",
                    hashCode(), currentTail, System.lineSeparator());
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
                if (!HEAD.compareAndSet(this, null, newTail)) {
                    throw new IllegalStateException("Could not replace null head with new tail");
                }
                if (!TAIL.compareAndSet(this, null, newTail)) {
                    throw new IllegalStateException("Could not replace null tail with new tail");
                }
                return newTail;
            }


            final var previousTail = TAIL.getAndSet(this, newTail);
            if (previousTail == currentTail) {
                if (!Segment.NEXT.compareAndSet(currentTail, null, newTail)) {
                    throw new IllegalStateException("Could not add new Segment after current tail, " +
                            "next node should be null");
                }
                if (finishWriteInSegments) {
                    currentTail.finishWrite();
                }
            } else {
                if (!HEAD.compareAndSet(this, null, newTail)) {
                    throw new IllegalStateException("Could not replace null head with new tail");
                }
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
