// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.pds.commons.core.PDSLogSanitizer;
import com.mercedesbenz.sechub.wrapper.prepare.LogSanitizerProvider;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareInputValidationSupport;

class InputValidationSupportTest implements LogSanitizerProvider {

    private static final Pattern PATTERN_ACCEPT_ALL = Pattern.compile(".*");

    private PrepareInputValidationSupport supportToTest;

    private PDSLogSanitizer logSanitizer;

    @BeforeEach
    void beforeEach() {
        this.logSanitizer = mock(PDSLogSanitizer.class);
    }

    @Test
    void builder_throws_exception_when_type_is_null() {
        /* prepare */
        Pattern locationPattern = PATTERN_ACCEPT_ALL;
        Pattern usernamePattern = PATTERN_ACCEPT_ALL;
        Pattern passwordPattern = PATTERN_ACCEPT_ALL;

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            /* @formatter:off */
            supportToTest = PrepareInputValidationSupport.builder().
                    setType(null).
                    setLogSanitizerProvider(this).
                    setLocationPattern(locationPattern).
                    setUserNamePattern(usernamePattern).
                    setPasswordPattern(passwordPattern).
                build();
            /* @formatter:on */
        });

        /* test */
        assertEquals("Type must not be null or empty.", exception.getMessage());
    }

    @Test
    void builder_throws_exception_when_type_is_empty() {
        /* prepare */
        Pattern locationPattern = PATTERN_ACCEPT_ALL;
        Pattern usernamePattern = PATTERN_ACCEPT_ALL;
        Pattern passwordPattern = PATTERN_ACCEPT_ALL;

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            /* @formatter:off */
            supportToTest = PrepareInputValidationSupport.builder().
                    setType("").
                    setLogSanitizerProvider(this).
                    setLocationPattern(locationPattern).
                    setUserNamePattern(usernamePattern).
                    setPasswordPattern(passwordPattern).
                build();
            /* @formatter:on */
        });

        /* test */
        assertEquals("Type must not be null or empty.", exception.getMessage());
    }

    @Test
    void builder_throws_exception_when_locationPattern_is_null() {
        /* prepare */
        Pattern usernamePattern = PATTERN_ACCEPT_ALL;
        Pattern passwordPattern = PATTERN_ACCEPT_ALL;

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            /* @formatter:off */
            supportToTest = PrepareInputValidationSupport.builder().
                    setType("type1").
                    setLogSanitizerProvider(this).
                    setLocationPattern(null).
                    setUserNamePattern(usernamePattern).
                    setPasswordPattern(passwordPattern).
                build();
            /* @formatter:on */
        });

        /* test */
        assertEquals("Pattern must not be null.", exception.getMessage());
    }

    @Test
    void builder_throws_exception_when_usernamePattern_is_null() {
        /* prepare */
        Pattern locationPattern = PATTERN_ACCEPT_ALL;
        Pattern passwordPattern = PATTERN_ACCEPT_ALL;

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            /* @formatter:off */
            supportToTest = PrepareInputValidationSupport.builder().
                    setType("type1").
                    setLogSanitizerProvider(this).
                    setLocationPattern(locationPattern).
                    setUserNamePattern(null).
                    setPasswordPattern(passwordPattern).
                build();
            /* @formatter:on */
        });

        /* test */
        assertEquals("Pattern must not be null.", exception.getMessage());
    }

    @Test
    void builder_throws_exception_when_passwordPattern_is_null() {
        /* prepare */
        Pattern locationPattern = PATTERN_ACCEPT_ALL;
        Pattern usernamePattern = PATTERN_ACCEPT_ALL;

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            /* @formatter:off */
            supportToTest = PrepareInputValidationSupport.builder().
                    setType("type1").
                    setLogSanitizerProvider(this).
                    setLocationPattern(locationPattern).
                    setUserNamePattern(usernamePattern).
                    setPasswordPattern(null).
                build();
            /* @formatter:on */
        });

        /* test */
        assertEquals("Pattern must not be null.", exception.getMessage());
    }

    @Test
    void builder_creates_instance_when_all_parameters_are_valid() {
        /* prepare */
        Pattern locationPattern = PATTERN_ACCEPT_ALL;
        Pattern usernamePattern = PATTERN_ACCEPT_ALL;
        Pattern passwordPattern = PATTERN_ACCEPT_ALL;

        /* execute */
        /* @formatter:off */
        supportToTest = PrepareInputValidationSupport.builder().
                setType("type1").
                setLogSanitizerProvider(this).
                setLocationPattern(locationPattern).
                setUserNamePattern(usernamePattern).
                setPasswordPattern(passwordPattern).
            build();
        /* @formatter:on */

        /* test */
        assertNotNull(supportToTest);
    }

    @Test
    void builder_creates_not_instance_when_log_sanitizer_provider_not_set() {
        /* prepare */
        Pattern locationPattern = PATTERN_ACCEPT_ALL;
        Pattern usernamePattern = PATTERN_ACCEPT_ALL;
        Pattern passwordPattern = PATTERN_ACCEPT_ALL;

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            /* @formatter:off */
            supportToTest = PrepareInputValidationSupport.builder().
                    setType("type1").
                    setLogSanitizerProvider(null).
                    setLocationPattern(locationPattern).
                    setUserNamePattern(usernamePattern).
                    setPasswordPattern(passwordPattern).
                build();
            /* @formatter:on */
        });

        /* test */
        assertEquals("Log sanitizer provider not defined", exception.getMessage());
    }

    @Override
    public PDSLogSanitizer getLogSanitizer() {
        return logSanitizer;
    }

}