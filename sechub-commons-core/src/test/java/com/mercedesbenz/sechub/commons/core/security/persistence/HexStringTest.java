package com.mercedesbenz.sechub.commons.core.security.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class HexStringTest {
    @Test
    void from_string() {
        String string = "Hello";
        String expectedString = "48656c6c6f";

        assertEquals(expectedString, new HexString(string).toString());
    }
    
    @Test
    void from_string_null_throw_illegal_argument_exception() {       
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new HexString((String)null);
        });

        assertEquals("String cannot be null.", exception.getMessage());
    }

    @Test
    void from_string_unicode() {
        String string = "Hello 🦄";
        String expectedString = "48656c6c6f20f09fa684";

        assertEquals(expectedString, new HexString(string).toString());
    }

    @Test
    void from_bytes() {
        byte[] bytes = "Hello".getBytes();
        String expectedString = "48656c6c6f";

        assertEquals(expectedString, new HexString(bytes).toString());
    }
    
    @Test
    void from_bytes_unicode() {
        byte[] bytes = "Hello 🦄".getBytes();
        String expectedString = "48656c6c6f20f09fa684";

        assertEquals(expectedString, new HexString(bytes).toString());
    }
    
    @Test
    void from_bytes_null_throw_illegal_argument_exception() {       
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new HexString((byte[])null);
        });

        assertEquals("Byte array cannot be null.", exception.getMessage());
    }
     
    @Test
    void toString_unicode_test() {
        String string = "Hello 🦄";
        String expectedString = "48656c6c6f20f09fa684";

        assertEquals(expectedString, new HexString(string).toString());
    }
    
    @Test
    void equals_test() {
        String string = "I like 🍍";
        
        HexString hexString = new HexString(string);
        HexString hexString2 = new HexString(string);
        
        assertEquals(hexString, hexString2);
        assertTrue(hexString.equals(hexString2));
        assertEquals(hexString.hashCode(), hexString2.hashCode());
    }
    
    @Test
    void test_immutablitiy() {
        String string = "A 🐺 in a 🐑 skin";
        HexString hexString = new HexString(string);
        
        assertFalse(string == hexString.toString());
        assertFalse(string.getBytes() == hexString.getBytes());
    }
}
