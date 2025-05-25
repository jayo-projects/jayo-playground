package jayo.playground.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BufferTest {
    @Test
    fun bufferToString() {
        assertEquals("Buffer(size=0)", Buffer.create4().toString())

        assertEquals(
            "Buffer(size=10 hex=610d0a620a630d645c65)",
            Buffer.create4().also { it.write("a\r\nb\nc\rd\\e") }.toString()
        )

        assertEquals(
            "Buffer(size=11 hex=547972616e6e6f73617572)",
            Buffer.create4().also { it.write("Tyrannosaur") }.toString()
        )

        assertEquals(
            "Buffer(size=64 hex=00000000000000000000000000000000000000000000000000000000000000000000000" +
                    "000000000000000000000000000000000000000000000000000000000)",
            Buffer.create4().also { it.write((0.toChar()).toString().repeat(64)) }.toString()
        )

        assertEquals(
            "Buffer(size=66 hex=000000000000000000000000000000000000000000000000000000000000" +
                    "00000000000000000000000000000000000000000000000000000000000000000000…)",
            Buffer.create4().also { it.write((0.toChar()).toString().repeat(66)) }.toString()
        )
    }
}
