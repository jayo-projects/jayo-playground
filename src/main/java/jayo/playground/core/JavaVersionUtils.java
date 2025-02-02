/*
 * Copyright (c) 2024-present, pull-vert and Jayo contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package jayo.playground.core;

import org.jspecify.annotations.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static java.lang.System.Logger.Level.INFO;

/**
 * Java 21 utils
 */
public final class JavaVersionUtils {
    private static final System.Logger LOGGER = System.getLogger("jayo.JavaVersionUtils");

    // un-instantiable
    private JavaVersionUtils() {
    }

    static {
        LOGGER.log(INFO, "Using Java 21 compatibility, virtual threads in use !");
    }

    /**
     * Java 21 has Virtual Thread support, so we use them
     */
    public static @NonNull ThreadFactory threadFactory(final @NonNull String prefix) {
        assert prefix != null;
        return Thread.ofVirtual()
                .name(prefix, 0)
                .inheritInheritableThreadLocals(true)
                .factory();
    }

    /**
     * Java 21 has Virtual Thread support, so we use them through
     * {@link Executors#newThreadPerTaskExecutor(ThreadFactory)} with our {@link #threadFactory(String)}
     */
    public static @NonNull ExecutorService executorService() {
        return Executors.newThreadPerTaskExecutor(threadFactory("JayoTaskRunnerThread-"));
    }
}
