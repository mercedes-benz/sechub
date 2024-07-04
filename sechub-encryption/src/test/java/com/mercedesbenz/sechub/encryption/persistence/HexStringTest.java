// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.encryption.persistence;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class HexStringTest {
    @Test
    void from_string() {
        /* prepare */
        String string = "Hello";
        String expectedString = "48656c6c6f";

        /* execute + test */
        assertEquals(expectedString, new HexString(string).toString());
    }

    @Test
    void from_string_null_throw_illegal_argument_exception() {
        /* execute */
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new HexString((String) null);
        });

        /* test */
        assertEquals("String cannot be null.", exception.getMessage());
    }

    @Test
    void from_string_unicode() {
        /* prepare */
        String string = "Hello 🦄";
        String expectedString = "48656c6c6f20f09fa684";

        /* execute + test */
        assertEquals(expectedString, new HexString(string).toString());
    }

    @Test
    void from_bytes() {
        /* prepare */
        byte[] bytes = "Hello".getBytes();
        String expectedString = "48656c6c6f";

        /* execute + test */
        assertEquals(expectedString, new HexString(bytes).toString());
    }

    @Test
    void from_bytes_unicode() {
        /* prepare */
        byte[] bytes = "Hello 🦄".getBytes();
        String expectedString = "48656c6c6f20f09fa684";

        /* execute + test */
        assertEquals(expectedString, new HexString(bytes).toString());
    }

    @Test
    void from_bytes_null_throw_illegal_argument_exception() {
        /* execute */
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new HexString((byte[]) null);
        });

        /* test */
        assertEquals("Byte array cannot be null.", exception.getMessage());
    }

    @Test
    void toString_unicode_test() {
        /* prepare */
        String string = "Hello 🦄";
        String expectedString = "48656c6c6f20f09fa684";

        /* execute + test */
        assertEquals(expectedString, new HexString(string).toString());
    }

    @Test
    void equals_test() {
        /* prepare */
        String string = "I like 🍍";

        /* execute */
        HexString hexString = new HexString(string);
        HexString hexString2 = new HexString(string);

        /* test */
        assertEquals(hexString, hexString2);
        assertTrue(hexString.equals(hexString2));
        assertEquals(hexString.hashCode(), hexString2.hashCode());
    }

    @Test
    void test_immutablitiy() {
        /* prepare */
        String string = "A 🐺 in a 🐑 skin";

        /* execute */
        HexString hexString = new HexString(string);

        /* test */
        assertFalse(string == hexString.toString());
        assertFalse(string.getBytes() == hexString.getBytes());
    }
}
