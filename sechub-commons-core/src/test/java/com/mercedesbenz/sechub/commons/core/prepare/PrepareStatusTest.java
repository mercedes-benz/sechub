package com.mercedesbenz.sechub.commons.core.prepare;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class PrepareStatusTest {

    @Test
    void from_string_ok() {
        assertEquals(PrepareStatus.OK, PrepareStatus.fromString("ok"));
        assertEquals(PrepareStatus.OK, PrepareStatus.fromString("OK"));
        assertEquals(PrepareStatus.OK, PrepareStatus.fromString("Ok"));
    }

    @Test
    void from_string_failed() {
        assertEquals(PrepareStatus.FAILED, PrepareStatus.fromString("failed"));
        assertEquals(PrepareStatus.FAILED, PrepareStatus.fromString("FAILED"));
        assertEquals(PrepareStatus.FAILED, PrepareStatus.fromString("Failed"));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = { "   ", "okay", "failure" })
    void from_string_unknown_returns_null(String string) {
        assertNull(PrepareStatus.fromString(string));
    }

}
