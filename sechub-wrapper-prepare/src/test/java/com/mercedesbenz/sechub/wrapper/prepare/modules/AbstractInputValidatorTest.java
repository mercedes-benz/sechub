package com.mercedesbenz.sechub.wrapper.prepare.modules;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class AbstractInputValidatorTest {

    TestInputValidator validatorToTest;

    @Test
    void constructor_throws_exception_when_type_is_null() {
        /* prepare */
        Pattern locationPattern = Pattern.compile(".*");
        Pattern usernamePattern = Pattern.compile(".*");
        Pattern passwordPattern = Pattern.compile(".*");

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            validatorToTest = new TestInputValidator(null, locationPattern, usernamePattern, passwordPattern);
        });

        /* test */
        assertEquals("Type must not be null or empty.", exception.getMessage());
    }

    @Test
    void constructor_throws_exception_when_type_is_empty() {
        /* prepare */
        Pattern locationPattern = Pattern.compile(".*");
        Pattern usernamePattern = Pattern.compile(".*");
        Pattern passwordPattern = Pattern.compile(".*");

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            validatorToTest = new TestInputValidator("", locationPattern, usernamePattern, passwordPattern);
        });

        /* test */
        assertEquals("Type must not be null or empty.", exception.getMessage());
    }

    @Test
    void constructor_throws_exception_when_locationPattern_is_null() {
        /* prepare */
        Pattern usernamePattern = Pattern.compile(".*");
        Pattern passwordPattern = Pattern.compile(".*");

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            validatorToTest = new TestInputValidator("type", null, usernamePattern, passwordPattern);
        });

        /* test */
        assertEquals("Pattern must not be null.", exception.getMessage());
    }

    @Test
    void constructor_throws_exception_when_usernamePattern_is_null() {
        /* prepare */
        Pattern locationPattern = Pattern.compile(".*");
        Pattern passwordPattern = Pattern.compile(".*");

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            validatorToTest = new TestInputValidator("type", locationPattern, null, passwordPattern);
        });

        /* test */
        assertEquals("Pattern must not be null.", exception.getMessage());
    }

    @Test
    void constructor_throws_exception_when_passwordPattern_is_null() {
        /* prepare */
        Pattern locationPattern = Pattern.compile(".*");
        Pattern usernamePattern = Pattern.compile(".*");

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            validatorToTest = new TestInputValidator("type", locationPattern, usernamePattern, null);
        });

        /* test */
        assertEquals("Pattern must not be null.", exception.getMessage());
    }

    @Test
    void constructor_creates_instance_when_all_parameters_are_valid() {
        /* prepare */
        Pattern locationPattern = Pattern.compile(".*");
        Pattern usernamePattern = Pattern.compile(".*");
        Pattern passwordPattern = Pattern.compile(".*");

        /* execute */
        validatorToTest = new TestInputValidator("type", locationPattern, usernamePattern, passwordPattern);

        /* test */
        assertNotNull(validatorToTest);
    }

    private class TestInputValidator extends AbstractInputValidator {
        public TestInputValidator(String type, Pattern locationPattern, Pattern usernamePattern, Pattern passwordPattern) {
            super(type, locationPattern, usernamePattern, passwordPattern);
        }
    }

}