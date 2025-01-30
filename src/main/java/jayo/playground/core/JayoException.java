/*
 * Copyright (c) 2024-present, pull-vert and Jayo contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package jayo.playground.core;

import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * Wraps an {@link IOException} with an unchecked exception.
 */
public class JayoException extends UncheckedIOException {
    /**
     * Constructs a {@link JayoException} with the specified detail message and cause.
     * <p>
     * Note that the detail message associated with {@code cause} is *not* automatically incorporated into this
     * exception's detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the {@code message} value)
     * @param cause   The {@link IOException} (which is saved for later retrieval by the {@code cause} value).
     */
    public JayoException(final @NonNull String message, final @NonNull IOException cause) {
        super(Objects.requireNonNull(message), Objects.requireNonNull(cause));
    }

    /**
     * Constructs a {@link JayoException} with the specified {@code cause} and a detail message of `cause.toString()`
     * (which typically contains the class and detail message of {@code cause}).
     *
     * @param cause The {@link IOException} (which is saved for later retrieval by the {@code cause} value).
     */
    public JayoException(final @NonNull IOException cause) {
        super(Objects.requireNonNull(cause).getMessage(), cause);
    }
}
