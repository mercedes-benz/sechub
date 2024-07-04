// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.encryption.persistence;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PlainStringTest {
    @Test
    void from_string() {
        /* prepare */
        String string = "Hello";

        /* execute + test */
        assertEquals(string, new PlainString(string).toString());
    }

    @Test
    void from_string_null_throw_illegal_argument_exception() {
        /* execute */
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new PlainString((String) null);
        });

        /* test */
        assertEquals("String cannot be null.", exception.getMessage());
    }

    @Test
    void from_string_unicode() {
        /* prepare */
        String string = "Hello 🦄";

        /* execute + test */
        assertEquals(string, new PlainString(string).toString());
    }

    @Test
    void from_bytes() {
        /* prepare */
        byte[] bytes = "Hello".getBytes();

        /* execute + test */
        assertEquals(new String(bytes), new PlainString(bytes).toString());
    }

    @Test
    void from_bytes_unicode() {
        /* prepare */
        byte[] bytes = "Hello 🦄".getBytes();

        /* execute + test */
        assertEquals(new String(bytes), new PlainString(bytes).toString());
    }

    @Test
    void from_bytes_null_throw_illegal_argument_exception() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new PlainString((byte[]) null);
        });

        /* execute + test */
        assertEquals("Byte array cannot be null.", exception.getMessage());
    }

    @Test
    void toString_unicode_test() {
        /* prepare */
        String string = "Hello 🦄";

        /* execute + test */
        assertEquals(string, new PlainString(string).toString());
    }

    @Test
    void equals_test() {
        /* prepare */
        String string = "I like 🍍";

        /* execute */
        PlainString plainString1 = new PlainString(string);
        PlainString plainString2 = new PlainString(string);

        /* test */
        assertEquals(plainString1, plainString2);
        assertTrue(plainString1.equals(plainString2));
        assertEquals(plainString1.hashCode(), plainString2.hashCode());
    }

    @Test
    void test_immutablitiy() {
        /* prepare */
        String string = "A 🐺 in a 🐑 skin";

        /* execute */
        PlainString plainString = new PlainString(string);

        /* test */
        assertFalse(string == plainString.toString());
        assertFalse(string.getBytes() == plainString.getBytes());
    }
}
