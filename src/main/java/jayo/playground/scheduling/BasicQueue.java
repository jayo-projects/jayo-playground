package jayo.playground.scheduling;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A basic FIFO queue that only supports {@link #offer(Object)}, {@link #poll()}, {@link #isEmpty()} and
 * {@link #iterator()}. All other methods throw {@code UnsupportedOperationException}.
 * <p>
 * <b>Be careful</b>, the returned boolean of our {@link #offer(Object)} method does not respect the
 * {@link Queue#offer(Object)} rationale, it has been adapted to our need. Read its javadoc for details.
 */
public interface BasicQueue<T> extends Queue<T> {
    /**
     * Inserts the specified element into this queue.
     * @return true if element is alone in this queue, meaning this queue was empty before that.
     * @apiNote This operation's result differs from the {@link Queue#offer(Object)} rationale. Our offer operation
     * always succeed.
     */
    @Override
    boolean offer(T t);

    @Override
    T poll();

    @Override
    boolean isEmpty();

    /**
     * @return an {@code Iterator} over the elements in this queue in the same order as they were inserted.
     */
    @NonNull
    @Override
    Iterator<T> iterator();

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

    @Override
    default Object @NonNull [] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    default <T1> T1 @NonNull [] toArray(@NonNull T1 @NonNull [] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    default <T1> T1 @NonNull [] toArray(@NonNull IntFunction<T1 @NonNull []> generator) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean remove(@Nullable Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean containsAll(@NonNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean addAll(@NonNull Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean removeAll(@NonNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean removeIf(@NonNull Predicate<? super T> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean retainAll(@NonNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    default @NonNull Spliterator<T> spliterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    default @NonNull Stream<T> stream() {
        throw new UnsupportedOperationException();
    }

    @Override
    default @NonNull Stream<T> parallelStream() {
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
