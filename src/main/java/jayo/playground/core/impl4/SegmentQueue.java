package jayo.playground.core.impl4;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

final class SegmentQueue {
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

        if (tail == null || !tail.owner || tail.limit + minimumCapacity > Segment.SIZE) {
            // Append a new empty segment to fill up.
            return addTail(SegmentPool.take());
        }
        return tail;
    }

    @NonNull
    Segment addTail(final @NonNull Segment newTail) {
        assert newTail != null;

        if (tail == null) {
            head = newTail;
        } else {
            tail.next = newTail;
        }
        tail = newTail;
        return newTail;
    }

    @Override
    public String toString() {
        return "SegmentQueue{" +
                "head=" + head +
                ", tail=" + tail +
                '}';
    }
}
