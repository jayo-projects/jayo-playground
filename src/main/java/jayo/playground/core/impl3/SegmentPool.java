/*
 * Copyright (c) 2024-present, pull-vert and Jayo contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 *
 * Forked from Okio (https://github.com/square/okio) and kotlinx-io (https://github.com/Kotlin/kotlinx-io), original
 * copyrights are below
 *
 * Copyright 2017-2023 JetBrains s.r.o. and respective authors and developers.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENCE file.
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
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class pools segments in a lock-free singly-linked queue of {@linkplain Segment segments}. Though this code is
 * lock-free it does use a sentinel {@link #DOOR} value to defend against races. To reduce the contention, the pool
 * consists of several buckets (see {@link #HASH_BUCKET_COUNT}), each holding a reference to its own segments cache.
 * Every {@link #take()} or {@link #recycle(Segment)} choose one of the buckets depending on a
 * {@link Thread#currentThread()}'s threadId.
 * <p>
 * On {@link #take()}, a caller swaps the Thread's corresponding segment cache with the {@link #DOOR} sentinel. If the
 * segment cache was not already locked, the caller pop the first segment from the cache.
 * <p>
 * On {@link #recycle(Segment)}, a caller swaps the head with a new node whose successor is the replaced head.
 * <p>
 * On conflict, operations are retried until they succeed.
 * <p>
 * This tracks the number of bytes in each queue in its {@code Segment.limit} property. Each element has a limit that's
 * one segment size greater than its successor element. The maximum size of the pool is a product of {@code #MAX_SIZE}
 * and {@code #HASH_BUCKET_COUNT}.
 * <p>
 * {@code #MAX_SIZE} is kept relatively small to avoid excessive memory consumption in case of a large
 * {@code #HASH_BUCKET_COUNT}.
 * For better handling of scenarios with high segments demand, a second-level pool is enabled and can be tuned by
 * setting up a value of `jayo.pool.size.bytes` system property.
 * <p>
 * The second-level pool use half of the {@code #HASH_BUCKET_COUNT} and if an initially selected bucket is empty on
 * {@link #take()} or full or {@link #recycle(Segment)}, all other buckets will be inspected before finally giving up
 * (which means allocating a new segment on {@link #take()}, or loosing a reference to a segment on
 * {@link #recycle(Segment)}). That second-level pool is used as a backup in case when {@link #take()} or
 * {@link #recycle(Segment)} failed due to an empty or exhausted segments chain in a corresponding first-level bucket
 * (one of {@code #HASH_BUCKET_COUNT}).
 */
@SuppressWarnings("unchecked")
public final class SegmentPool {
    // un-instantiable
    private SegmentPool() {
    }

    /**
     * The maximum number of segments to pool per hash bucket.
     */
    private static final int FIRST_LEVEL_POOL_LENGTH = 8;
    private static final int SECOND_LEVEL_POOL_LENGTH = 256;

    /**
     * For tests only: the maximum number of bytes to pool per hash bucket.
     */
    // TODO: Is this a good maximum size?
    static final int FIRST_LEVEL_POOL_MAX_BYTE_SIZE = FIRST_LEVEL_POOL_LENGTH * Segment.SIZE; // ~150 KiB.
    private static final int DEFAULT_SECOND_LEVEL_POOL_TOTAL_SIZE = 4 * 1024 * 1024; // 4MB

    /**
     * The number of hash buckets. This number needs to balance keeping the pool small and contention low. We use the
     * number of processors rounded up to the nearest power of two.
     * For example a machine with 6 cores will have 8 hash buckets.
     */
    private static final int HASH_BUCKET_COUNT =
            Integer.highestOneBit(Runtime.getRuntime().availableProcessors() * 2 - 1);

    private static final int HASH_BUCKET_COUNT_L2;

    /**
     * A sentinel segment cache to indicate that the cache is currently being modified.
     */
    private static final @NonNull SegmentCache DOOR = new SegmentCache(0);

    /**
     * Hash buckets each contain a singly linked queue of segments. The index/key is a hash function of thread ID
     * because it may reduce contention or increase locality.
     * <p>
     * We don't use ThreadLocal because we don't know how many threads the host process has, and we don't want to leak
     * memory for the duration of a thread's life.
     */
    private static final @NonNull AtomicReference<@Nullable SegmentCache> @NonNull [] HASH_BUCKETS;
    private static final @NonNull AtomicReference<@Nullable SegmentCache> @NonNull [] HASH_BUCKETS_L2;

    static {
        final var hashBucketCountL2 = HASH_BUCKET_COUNT / 2;
        HASH_BUCKET_COUNT_L2 = (hashBucketCountL2 > 0) ? hashBucketCountL2 : 1;

        HASH_BUCKETS = new AtomicReference[HASH_BUCKET_COUNT];
        // null value implies an empty bucket
        Arrays.setAll(HASH_BUCKETS, _unused -> new AtomicReference<@Nullable SegmentCache>());

        HASH_BUCKETS_L2 = new AtomicReference[HASH_BUCKET_COUNT_L2];
        // null value implies an empty bucket
        Arrays.setAll(HASH_BUCKETS_L2, _unused -> new AtomicReference<@Nullable SegmentCache>());
    }

    static @NonNull Segment take() {
        final var cacheRef = HASH_BUCKETS[l1BucketId(Thread.currentThread())];

        while (true) {
            // Hold the door !!!
            final var cache = cacheRef.getAndSet(DOOR);
            if (cache == DOOR) {
                // We didn't acquire the lock. Let's try again
                continue;
            }

            if (cache == null || cache.isEmpty()) {
                // We acquired the lock, but the cache was empty.
                // Unlock and acquire a segment from the second-level cache
                cacheRef.set(null);

                return takeL2();
            }

            // We acquired the lock and the cache was not empty. Pop the first element and return it.
            final var segment = cache.pop();
            cacheRef.set(cache);
            segment.byteBuffer.clear();
            segment.byteBuffer.limit(0);
            return segment;
        }
    }

    private static @NonNull Segment takeL2() {
        var bucketId = l2BucketId(Thread.currentThread());
        var attempts = 0;

        while (true) {
            final var cacheRef = HASH_BUCKETS_L2[bucketId];

            // Hold the door !!!
            final var cache = cacheRef.getAndSet(DOOR);
            if (cache == DOOR) {
                // We didn't acquire the lock. Let's try again
                continue;
            }

            if (cache == null || cache.isEmpty()) {
                // We acquired the lock, but the cache was empty.
                // Unlock the current bucket and select a new one.
                // If all buckets were already scanned, allocate a new segment.
                cacheRef.set(null);

                if (attempts < HASH_BUCKET_COUNT_L2) {
                    bucketId = (bucketId + 1) & (HASH_BUCKET_COUNT_L2 - 1);
                    attempts++;
                    continue;
                }

                return new Segment();
            }

            // We acquired the lock and the pool was not empty. Pop the first element and return it.
            final var segment = cache.pop();
            cacheRef.set(cache);
            segment.byteBuffer.clear();
            segment.byteBuffer.limit(0);
            return segment;
        }
    }

    static void recycle(final @NonNull Segment segment) {
        assert segment != null;

        final var segmentCopyTracker = segment.copyTracker;

        if (segmentCopyTracker.removeCopy()) {
            return; // This segment cannot be recycled.
        }
        final var toRecycle = segmentCopyTracker.origin;

        final var cacheRef = HASH_BUCKETS[l1BucketId(Thread.currentThread())];

        var cache = cacheRef.get();
        if (cache == DOOR) {
            return; // A take() is currently in progress.
        }

        // cache was null, create it
        if (cache == null) {
            cache = new SegmentCache(FIRST_LEVEL_POOL_LENGTH);
        }

        final var cached = cache.push(toRecycle);
        cacheRef.set(cache);

        if (!cached) {
            recycleL2(toRecycle);
        }
    }

    private static void recycleL2(final @NonNull Segment segment) {
        var bucketId = l2BucketId(Thread.currentThread());
        var attempts = 0;

        var done = false;
        while (!done) {
            final var cacheRef = HASH_BUCKETS_L2[bucketId];
            var cache = cacheRef.get();

            if (cache == DOOR) {
                continue; // A take() is currently in progress.
            }

            // cache was null, create it
            if (cache == null) {
                cache = new SegmentCache(SECOND_LEVEL_POOL_LENGTH);
            }

            if (cache.push(segment) // successful cache
                    || attempts >= HASH_BUCKET_COUNT_L2) { // L2 pool is full.
                done = true;
            } else {
                // The current bucket is full, try to find another one and return the segment there.
                attempts++;
                bucketId = (bucketId + 1) & (HASH_BUCKET_COUNT_L2 - 1);
            }

            cacheRef.set(cache);
        }
    }

    static int l1BucketId(final @NonNull Thread thread) {
        return bucketId(thread, HASH_BUCKET_COUNT - 1L);
    }

    private static int l2BucketId(final @NonNull Thread thread) {
        return bucketId(thread, HASH_BUCKET_COUNT_L2 - 1L);
    }

    static int bucketId(final @NonNull Thread thread, final long mask) {
        Objects.requireNonNull(thread);
        return (int) (thread.threadId() & mask);
    }

    /**
     * A simple cache of segments.
     */
    private static class SegmentCache {
        private final int cacheSize;
        /**
         * the array of elements
         */
        private final @Nullable Segment @NonNull [] segments;

        /**
         * the number of elements in the cache
         */
        private int count = 0;

        /**
         * the index of the first valid element (undefined if count == 0)
         */
        private int start = 0;

        private SegmentCache(final int cacheSize) {
            this.cacheSize = cacheSize;
            segments = new Segment[cacheSize];
        }

        private boolean isEmpty() {
            return count == 0;
        }

        private @NonNull Segment pop() {
            final var segment = segments[start];
            segments[start] = null;
            start = (start + 1) % cacheSize;
            count--;
            assert segment != null;
            return segment;
        }

        private boolean push(final @NonNull Segment segment) {
            assert segment != null;

            if (count >= cacheSize) {
                return false;
            }
            final var next = (start + count) % cacheSize;
            segments[next] = segment;
            count++;
            return true;
        }

        @Override
        public String toString() {
            return "SegmentCache{" +
                    "cacheSize=" + cacheSize +
                    ", segments=" + Arrays.toString(segments) +
                    ", count=" + count +
                    ", start=" + start +
                    '}';
        }
    }
}
