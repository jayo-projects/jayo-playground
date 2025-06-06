/*
 * Copyright (c) 2025-present, pull-vert and Jayo contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package jayo.playground.scheduling.impl5;

import jayo.playground.scheduling.BasicFifoQueue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class SinglyLinkedBasicFifoQueue<T> implements BasicFifoQueue<T> {
    private static class Node<T> {
        private final @NonNull T value;
        private @Nullable Node<T> next = null;

        private Node(final @NonNull T value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "value=" + value +
                    ", next=" + next +
                    '}';
        }
    }

    private @Nullable Node<T> head = null;
    private @Nullable Node<T> tail = null;

    @Override
    public boolean offer(final @NonNull T item) {
        final var node = new Node<>(item);
        if (tail != null) {
            tail.next = node;
            tail = node;
            return false;
        }
        // queue was empty
        tail = node;
        head = node;
        return true;
    }

    @Override
    public T peek() {
        return head != null ? head.value : null;
    }

    @Override
    public T peekLast() {
        return tail != null ? tail.value : null;
    }

    @Override
    public T poll() {
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
        return (head != null) ? head.value : null;
    }

    @Override
    public boolean isEmpty() {
        return head == null;
    }

    @Override
    public boolean contains(Object o) {
        var node = head;
        while (node != null) {
            if (node.value.equals(o)) {
                return true;
            }
            node = node.next;
        }
        return false;
    }

    @Override
    public boolean remove(@Nullable Object o) {
        if (head == null) {
            return false;
        }
        var node = head;
        if (node.value.equals(o)) {
            head = node.next;
            if (tail == node) {
                tail = null;
            }
            return true;
        }

        var previous = head;
        node = head.next;
        while (node != null) {
            if (node.value.equals(o)) {
                if (tail == node) {
                    tail = previous;
                }
                previous.next = node.next;
                return true;
            }
            previous = node;
            node = node.next;
        }
        return false;
    }

    @Override
    public @NonNull Iterator<T> iterator() {
        return head != null ? new SinglyLinkedIterator() : Collections.emptyIterator();
    }

    private final class SinglyLinkedIterator implements Iterator<T> {
        private final @NonNull Node<T> virtualOrigin = new Node<>(null); // ok to put null, this first item is virtual
        private @NonNull Node<T> previous;
        private @NonNull Node<T> current;
        private boolean canRemove = false;

        private SinglyLinkedIterator() {
            previous = virtualOrigin;
            current = previous;
            current.next = head;
        }

        @Override
        public boolean hasNext() {
            canRemove = false;
            return current.next != null;
        }

        @Override
        public T next() {
            if (current.next == null) {
                throw new NoSuchElementException();
            }
            previous = current;
            current = current.next;
            canRemove = true;
            return current.value;
        }

        @Override
        public void remove() {
            if (!canRemove) {
                throw new IllegalStateException();
            }
            if (head == current) {
                head = current.next;
            }
            if (tail == current) {
                tail = previous != virtualOrigin ? previous : null;
            }
            previous.next = current.next;
            current = previous;
            canRemove = false;
        }
    }

    @Override
    public String toString() {
        return "SinglyLinkedBasicFifoQueue{" +
                "head=" + head +
                ", tail=" + tail +
                '}';
    }
}
