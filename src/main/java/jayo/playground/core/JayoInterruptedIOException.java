/*
 * Copyright (c) 2024-present, pull-vert and Jayo contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package jayo.playground.core;

import org.jspecify.annotations.NonNull;

import java.io.InterruptedIOException;
import java.util.Objects;

/**
 * Signals that an I/O operation has been interrupted. A {@code JayoInterruptedIOException} is thrown to indicate that
 * an input or output transfer has been terminated because the thread performing it was interrupted.
 * <p>
 * Wraps an {@link InterruptedIOException} with an unchecked exception.
 */
public final class JayoInterruptedIOException extends JayoException {
    public JayoInterruptedIOException(final @NonNull String message) {
        super(Objects.requireNonNull(message), new InterruptedIOException(message));
    }
}
