// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.encryption.persistence;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class BinaryStringFactoryTest {
    @Test
    void create_from_string_no_type_given() {
        /* prepare */
        String string = "hello";

        /* execute */
        BinaryString binaryString = BinaryStringFactory.createFromString(string);

        /* test */
        assertNotNull(binaryString);
        assertEquals(binaryString.getType(), BinaryStringEncodingType.BASE64);
        assertTrue(binaryString instanceof Base64String);
        assertEquals("aGVsbG8=", binaryString.toString());
    }

    @Test
    void create_from_string_base64_type_given() {
        /* prepare */
        String string = "Hello 🌌!";

        /* execute */
        BinaryString binaryString = BinaryStringFactory.createFromString(string, BinaryStringEncodingType.BASE64);

        /* test */
        assertNotNull(binaryString);
        assertEquals(binaryString.getType(), BinaryStringEncodingType.BASE64);
        assertTrue(binaryString instanceof Base64String);
        assertEquals("SGVsbG8g8J+MjCE=", binaryString.toString());
    }

    @Test
    void create_from_base64_no_type_given() {
        /* prepare */
        String string = "SGVsbG8g8J+MjCE=";

        /* execute */
        BinaryString binaryString = BinaryStringFactory.createFromBase64(string);

        /* test */
        assertNotNull(binaryString);
        assertEquals(binaryString.getType(), BinaryStringEncodingType.BASE64);
        assertTrue(binaryString instanceof Base64String);
        assertEquals("SGVsbG8g8J+MjCE=", binaryString.toString());
    }

    @Test
    void createFromBase64_null_input() {
        /* execute */
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            BinaryStringFactory.createFromBase64(null);
        });

        /* test */
        assertEquals("String cannot be null.", exception.getMessage());
    }

    @Test
    void createFromHex_null_input() {
        /* execute */
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            BinaryStringFactory.createFromHex(null);
        });

        /* test */
        assertEquals("String cannot be null.", exception.getMessage());
    }

    @Test
    void create_from_string_plain_type_given() {
        /* prepare */
        String string = "hello";

        /* execute */
        BinaryString binaryString = BinaryStringFactory.createFromString(string, BinaryStringEncodingType.PLAIN);

        /* test */
        assertNotNull(binaryString);
        assertEquals(binaryString.getType(), BinaryStringEncodingType.PLAIN);
        assertTrue(binaryString instanceof PlainString);
        assertEquals("hello", binaryString.toString());
    }

    @Test
    void create_from_string_hex_type_given() {
        /* prepare */
        String string = "hello";

        /* execute */
        BinaryString binaryString = BinaryStringFactory.createFromString(string, BinaryStringEncodingType.HEX);

        /* test */
        assertNotNull(binaryString);
        assertEquals(BinaryStringEncodingType.HEX, binaryString.getType());
        assertTrue(binaryString instanceof HexString);
        assertEquals("68656c6c6f", binaryString.toString());
    }

    @Test
    void create_from_string_no_type_given_and_input_null() {
        /* prepare */
        String string = null;

        /* execute */
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            BinaryStringFactory.createFromString(string);
        });

        /* test */
        assertEquals("String cannot be null.", exception.getMessage());
    }

    @Test
    void create_from_bytes_no_type_given() {
        /* prepare */
        byte[] bytes = new byte[] { 104, 101, 108, 108, 111 };

        /* execute */
        BinaryString binaryString = BinaryStringFactory.createFromBytes(bytes);

        /* test */
        assertNotNull(binaryString);
        assertEquals(BinaryStringEncodingType.BASE64, binaryString.getType());
        assertTrue(binaryString instanceof Base64String);
        assertEquals("aGVsbG8=", binaryString.toString());
    }

    @Test
    void create_from_bytes_base64_type_given() {
        /* prepare */
        byte[] bytes = new byte[] { 104, 101, 108, 108, 111 };

        /* execute */
        BinaryString binaryString = BinaryStringFactory.createFromBytes(bytes, BinaryStringEncodingType.BASE64);

        /* test */
        assertNotNull(binaryString);
        assertEquals(BinaryStringEncodingType.BASE64, binaryString.getType());
        assertTrue(binaryString instanceof Base64String);
        assertEquals("aGVsbG8=", binaryString.toString());
    }

    @Test
    void create_from_bytes_plain_type_given() {
        /* prepare */
        byte[] bytes = new byte[] { 104, 101, 108, 108, 111 };

        /* execute */
        BinaryString binaryString = BinaryStringFactory.createFromBytes(bytes, BinaryStringEncodingType.PLAIN);

        /* test */
        assertNotNull(binaryString);
        assertEquals(BinaryStringEncodingType.PLAIN, binaryString.getType());
        assertTrue(binaryString instanceof PlainString);
        assertEquals("hello", binaryString.toString());
    }

    @Test
    void create_from_bytes_hex_type_given() {
        /* prepare */
        byte[] bytes = new byte[] { 104, 101, 108, 108, 111 };

        /* execute */
        BinaryString binaryString = BinaryStringFactory.createFromBytes(bytes, BinaryStringEncodingType.HEX);

        /* test */
        assertNotNull(binaryString);
        assertEquals(BinaryStringEncodingType.HEX, binaryString.getType());
        assertTrue(binaryString instanceof HexString);
        assertEquals("68656c6c6f", binaryString.toString());
    }

    @Test
    void create_from_bytes_no_type_given_and_input_null() {
        /* prepare */
        byte[] bytes = null;

        /* execute */
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            BinaryStringFactory.createFromBytes(bytes);
        });

        /* test */
        assertEquals("String cannot be null.", exception.getMessage());
    }

    @Test
    void createFromHex_from_string_plain_type_given() {
        /* prepare */
        String string = "68656c6c6f";

        /* execute */
        BinaryString binaryString = BinaryStringFactory.createFromHex(string, BinaryStringEncodingType.PLAIN);

        /* test */
        assertNotNull(binaryString);
        assertEquals(BinaryStringEncodingType.PLAIN, binaryString.getType());
        assertTrue(binaryString instanceof PlainString);
        assertEquals("hello", binaryString.toString());
    }

    @Test
    void createFromHex_base64_type_given() {
        /* prepare */
        String string = "68656c6c6f";

        /* execute */
        BinaryString binaryString = BinaryStringFactory.createFromHex(string, BinaryStringEncodingType.BASE64);

        /* test */
        assertNotNull(binaryString);
        assertEquals(BinaryStringEncodingType.BASE64, binaryString.getType());
        assertTrue(binaryString instanceof Base64String);
        assertEquals("aGVsbG8=", binaryString.toString());
    }

    @Test
    void createFromHex_no_type_given() {
        /* prepare */
        String string = "68656c6c6f";

        /* execute */
        BinaryString binaryString = BinaryStringFactory.createFromHex(string);

        /* test */
        assertNotNull(binaryString);
        assertEquals(BinaryStringEncodingType.BASE64, binaryString.getType());
        assertTrue(binaryString instanceof Base64String);
        assertEquals("aGVsbG8=", binaryString.toString());
    }

    @Test
    void createFromBase64_plain_type_given() {
        /* prepare */
        String string = "aGVsbG8=";

        /* execute */
        BinaryString binaryString = BinaryStringFactory.createFromBase64(string, BinaryStringEncodingType.PLAIN);

        /* test */
        assertNotNull(binaryString);
        assertEquals(BinaryStringEncodingType.PLAIN, binaryString.getType());
        assertTrue(binaryString instanceof PlainString);
        assertEquals("hello", binaryString.toString());
    }

    @Test
    void createFromBase64_hex_type_given() {
        /* prepare */
        String string = "aGVsbG8=";

        /* execute */
        BinaryString binaryString = BinaryStringFactory.createFromBase64(string, BinaryStringEncodingType.HEX);

        /* test */
        assertNotNull(binaryString);
        assertEquals(BinaryStringEncodingType.HEX, binaryString.getType());
        assertTrue(binaryString instanceof HexString);
        assertEquals("68656c6c6f", binaryString.toString());
    }
}
