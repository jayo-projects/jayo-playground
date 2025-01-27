package jayo.playground.scheduling;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface BasicQueue<T> extends Queue<T> {
    @Override
    boolean isEmpty();

    @Override
    T poll();

    /**
     * This operation's result differs from {@link Queue} rationale. Offer operation always succeed.
     * @return true if element is alone in this queue, meaning this queue was empty before that.
     */
    @Override
    boolean offer(T t);

    @NotNull
    @Override
    default Iterator<T> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    default void forEach(Consumer<? super T> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    default int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    default Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    default <T1> T1[] toArray(@NotNull T1[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    default <T1> T1[] toArray(@NotNull IntFunction<T1[]> generator) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean containsAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean addAll(@NotNull Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean removeIf(@NotNull Predicate<? super T> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void clear() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    default Spliterator<T> spliterator() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    default Stream<T> stream() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    default Stream<T> parallelStream() {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    default T remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    default T element() {
        throw new UnsupportedOperationException();
    }

    @Override
    default T peek() {
        throw new UnsupportedOperationException();
    }
}
