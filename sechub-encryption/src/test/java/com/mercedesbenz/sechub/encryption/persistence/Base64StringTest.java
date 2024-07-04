// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.encryption.persistence;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class Base64StringTest {
    @Test
    void from_string() {
        /* prepare */
        String string = "Hello";
        String expectedString = "SGVsbG8=";

        /* execute + test */
        assertEquals(expectedString, new Base64String(string).toString());
    }

    @Test
    void from_string_null_throw_illegal_argument_exception() {
        /* execute */
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Base64String((String) null);
        });

        /* test */
        assertEquals("String cannot be null.", exception.getMessage());
    }

    @Test
    void from_string_unicode() {
        String string = "Hello 🦄";
        String expectedString = "SGVsbG8g8J+mhA==";

        assertEquals(expectedString, new Base64String(string).toString());
    }

    @Test
    void from_bytes() {
        byte[] bytes = "Hello".getBytes();
        String expectedString = "SGVsbG8=";

        assertEquals(expectedString, new Base64String(bytes).toString());
    }

    @Test
    void from_bytes_unicode() {
        byte[] bytes = "Hello 🦄".getBytes();
        String expectedString = "SGVsbG8g8J+mhA==";

        assertEquals(expectedString, new Base64String(bytes).toString());
    }

    @Test
    void from_bytes_null_throw_illegal_argument_exception() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Base64String((byte[]) null);
        });

        assertEquals("Byte array cannot be null.", exception.getMessage());
    }

    @Test
    void toString_unicode_test() {
        String string = "Hello 🦄";
        String expectedString = "SGVsbG8g8J+mhA==";

        assertEquals(expectedString, new Base64String(string).toString());
    }

    @Test
    void equals_test() {
        String string = "I like 🍍";

        Base64String b64String = new Base64String(string);
        Base64String b64String2 = new Base64String(string);

        assertEquals(b64String, b64String2);
        assertTrue(b64String.equals(b64String2));
        assertEquals(b64String.hashCode(), b64String2.hashCode());
    }

    @Test
    void test_immutablitiy() {
        String string = "A 🐺 in a 🐑 skin";
        Base64String b64String = new Base64String(string);

        assertFalse(string == b64String.toString());
        assertFalse(string.getBytes() == b64String.getBytes());
    }
}
