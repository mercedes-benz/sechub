package com.mercedesbenz.sechub.sharedkernel.messaging;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class LocalDateTimeMessageDataProviderTest {

    private LocalDateTimeMessageDataProvider providerToTest;

    @BeforeEach
    void beforeEach() {
        providerToTest = new LocalDateTimeMessageDataProvider();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "", "illegal", "2011-12-03T10", "2011-12T10:15:30" })
    void get_illegal_values_return_null(String notLegalIso8601) {
        assertNull(providerToTest.get(notLegalIso8601));
    }

    @ParameterizedTest
    @ValueSource(strings = { "2011-12-03T10:15:30", "2423-01-13T03:06:12" })
    void get_legal_values_return_correct_date_time(String legalIso8601) {
        /* execute */
        LocalDateTime result = providerToTest.get(legalIso8601);

        /* test */
        LocalDateTime expected = LocalDateTime.parse(legalIso8601);
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "2011-12-03T10:15:30", "2423-01-13T03:06:12" })
    void getString_legal_values_return_correct_date_time(String expectedLegalIso8601) {
        /* prepare */
        LocalDateTime parameterAsObject = LocalDateTime.parse(expectedLegalIso8601);

        /* execute */
        String result = providerToTest.getString(parameterAsObject);

        /* test */
        assertNotNull(result);
        assertEquals(expectedLegalIso8601, result);
    }

    @Test
    void getString_null_returns_null() {
        assertEquals(null, providerToTest.getString(null));
    }
}
