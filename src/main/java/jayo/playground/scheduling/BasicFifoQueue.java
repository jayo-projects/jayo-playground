/*
 * Copyright (c) 2025-present, pull-vert and Jayo contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package jayo.playground.scheduling;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A basic FIFO queue that only implements {@link #offer(Object)}, {@link #poll()}, {@link #isEmpty()} and
 * {@link #iterator()} of {@linkplain Queue j.u.Queue}. All other methods throw {@code UnsupportedOperationException}.
 * This interface also adds the single additional {@link #peekLast()} method.
 * <p>
 * <b>Be careful</b>, our {@link #offer(Object)} and {@link #poll()} methods do not respect the {@link Queue} rationale,
 * they have been adapted to our need. Read its javadoc for details.
 */
public interface BasicFifoQueue<T> extends Queue<T> {
    static <T> BasicFifoQueue<T> create() {
        return new SinglyLinkedBasicFifoQueue<>();
    }

    /**
     * Inserts the specified element into this queue.
     *
     * @return true if the item is alone in this queue, meaning this queue was empty before that.
     * @apiNote This operation's result differs from the {@link Queue#offer(Object)} rationale. Our offer operation
     * always succeeds.
     */
    @Override
    boolean offer(final @NonNull T item);

    @Override
    @Nullable T peek();

    /**
     * Retrieves, but does not remove, the last element of this queue, or returns {@code null} if this queue is empty.
     *
     * @return the tail of this queue, or {@code null} if this queue is empty
     */
    @Nullable T peekLast();

    /**
     * Removes the first element of this queue and returns its successor.
     * @return the new head of this queue, or {@code null} if this queue is now empty.
     * @apiNote This operation's result differs from the {@link Queue#poll()} rationale. We return the new head instead
     * of the previous one that was removed.
     */
    @Override
    @Nullable T poll();

    @Override
    boolean isEmpty();

    @Override
    boolean contains(Object o);

    @Override
    boolean remove(@Nullable Object o);

    /**
     * @return an {@code Iterator} over the elements in this queue in the same order as they were inserted.
     */
    @NonNull
    @Override
    Iterator<T> iterator();

    @Override
    default void forEach(Consumer<? super T> action) {
        System.out.println("Big Problem");
        throw new UnsupportedOperationException();
    }

    @Override
    default int size() {
        System.out.println("Big Problem");
        throw new UnsupportedOperationException();
    }

    @Override
    default Object @NonNull [] toArray() {
        System.out.println("Big Problem");
        throw new UnsupportedOperationException();
    }

    @Override
    default <T1> T1 @NonNull [] toArray(@NonNull T1 @NonNull [] a) {
        System.out.println("Big Problem");
        throw new UnsupportedOperationException();
    }

    @Override
    default <T1> T1 @NonNull [] toArray(@NonNull IntFunction<T1 @NonNull []> generator) {
        System.out.println("Big Problem");
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean containsAll(@NonNull Collection<?> c) {
        System.out.println("Big Problem");
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean addAll(@NonNull Collection<? extends T> c) {
        System.out.println("Big Problem");
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean removeAll(@NonNull Collection<?> c) {
        System.out.println("Big Problem");
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean removeIf(@NonNull Predicate<? super T> filter) {
        System.out.println("Big Problem");
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean retainAll(@NonNull Collection<?> c) {
        System.out.println("Big Problem");
        throw new UnsupportedOperationException();
    }

    @Override
    default void clear() {
        System.out.println("Big Problem");
        throw new UnsupportedOperationException();
    }

    @Override
    default @NonNull Spliterator<T> spliterator() {
        System.out.println("Big Problem");
        throw new UnsupportedOperationException();
    }

    @Override
    default @NonNull Stream<T> stream() {
        System.out.println("Big Problem");
        throw new UnsupportedOperationException();
    }

    @Override
    default @NonNull Stream<T> parallelStream() {
        System.out.println("Big Problem");
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean add(T t) {
        System.out.println("Big Problem");
        throw new UnsupportedOperationException();
    }

    @Override
    default T remove() {
        System.out.println("Big Problem");
        throw new UnsupportedOperationException();
    }

    @Override
    default T element() {
        System.out.println("Big Problem");
        throw new UnsupportedOperationException();
    }
}
