package com.mercedesbenz.sechub.commons.core.security.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class PlainStringTest {
    @Test
    void from_string() {
        String string = "Hello";

        assertEquals(string, new PlainString(string).toString());
    }
    
    @Test
    void from_string_null_throw_illegal_argument_exception() {       
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new PlainString((String)null);
        });

        assertEquals("String cannot be null.", exception.getMessage());
    }

    @Test
    void from_string_unicode() {
        String string = "Hello 🦄";

        assertEquals(string, new PlainString(string).toString());
    }

    @Test
    void from_bytes() {
        byte[] bytes = "Hello".getBytes();

        assertEquals(new String(bytes), new PlainString(bytes).toString());
    }
    
    @Test
    void from_bytes_unicode() {
        byte[] bytes = "Hello 🦄".getBytes();

        assertEquals(new String(bytes), new PlainString(bytes).toString());
    }
    
    @Test
    void from_bytes_null_throw_illegal_argument_exception() {       
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new PlainString((byte[])null);
        });

        assertEquals("Byte array cannot be null.", exception.getMessage());
    }
     
    @Test
    void toString_unicode_test() {
        String string = "Hello 🦄";

        assertEquals(string, new PlainString(string).toString());
    }
    
    @Test
    void equals_test() {
        String string = "I like 🍍";
        
        PlainString plainString1 = new PlainString(string);
        PlainString plainString2 = new PlainString(string);
        
        assertEquals(plainString1, plainString2);
        assertTrue(plainString1.equals(plainString2));
        assertEquals(plainString1.hashCode(), plainString2.hashCode());
    }
    
    @Test
    void test_immutablitiy() {
        String string = "A 🐺 in a 🐑 skin";
        PlainString plainString = new PlainString(string);
        
        assertFalse(string == plainString.toString());
        assertFalse(string.getBytes() == plainString.getBytes());
    }
}
