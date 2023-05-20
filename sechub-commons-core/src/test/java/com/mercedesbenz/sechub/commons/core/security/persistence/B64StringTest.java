package com.mercedesbenz.sechub.commons.core.security.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class B64StringTest {
    @Test
    void fromString() {
        String string = "Hello";
        String expectedString = "SGVsbG8=";

        assertEquals(expectedString, B64String.from(string).toString());
    }

    @Test
    void fromString_unicode() {
        String string = "Hello 🦄";
        String expectedString = "SGVsbG8g8J+mhA==";

        assertEquals(expectedString, B64String.from(string).toString());
    }

    @Test
    void fromBytes() {
        byte[] bytes = "Hello".getBytes();
        String expectedString = "SGVsbG8=";

        assertEquals(expectedString, B64String.from(bytes).toString());
    }
}
