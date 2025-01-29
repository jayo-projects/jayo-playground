/*
 * Copyright (c) 2025-present, pull-vert and Jayo contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 *
 * Forked from OkHttp (https://github.com/square/okhttp), original copyright is below
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

package jayo.playground.scheduling;

import jayo.playground.scheduling.impl1.TaskRunner1;
import jayo.playground.scheduling.impl2.TaskRunner2;
import jayo.playground.scheduling.impl3.TaskRunner3;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * A set of worker threads that are shared among a set of task queues.
 * <p>
 * Most applications should share a process-wide {@link TaskRunner}, and create queues for per-client work with
 * {@link #newQueue()}.
 *
 * @implNote The task runner is also responsible for releasing held threads when the library is unloaded. This is for
 * the benefit of container environments that implement code unloading.
 */
public interface TaskRunner {
    static TaskRunner create1(final @NonNull ExecutorService executor) {
        Objects.requireNonNull(executor);
        return new TaskRunner1(executor);
    }

    static TaskRunner create2(final @NonNull ExecutorService executor) {
        Objects.requireNonNull(executor);
        return new TaskRunner2(executor);
    }

    static TaskRunner create3(final @NonNull ExecutorService executor) {
        Objects.requireNonNull(executor);
        return new TaskRunner3(executor);
    }

    @NonNull
    TaskQueue newQueue();

    @NonNull
    ScheduledTaskQueue newScheduledQueue();

    void execute(final boolean cancellable, final Runnable block);

    void shutdown();
}
