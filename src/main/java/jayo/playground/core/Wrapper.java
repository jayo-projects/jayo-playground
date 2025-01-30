/*
 * Copyright (c) 2024-present, pull-vert and Jayo contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package jayo.playground.core;

public final class Wrapper {
    // un-instantiable
    private Wrapper() {
    }

    public static final class Int {
        public int value;

        public Int(final int value) {
            this.value = value;
        }
    }
}
