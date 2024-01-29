// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.job;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ExecutionResultTest {

    @ParameterizedTest
    @EnumSource(value = ExecutionResult.class)
    void fromString_every_enum_name_is_treated_even_lowercased(ExecutionResult data) {
        String value = data.name();

        ExecutionResult result = ExecutionResult.fromString(value);
        assertEquals(data, result);

        result = ExecutionResult.fromString(value.toLowerCase());
        assertEquals(data, result);

    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    @ValueSource(strings = { "unknown" })
    void fromString_every_unknown_name_is_returned_as_nullnull(String data) {

        ExecutionResult result = ExecutionResult.fromString(data);
        assertNull(result);

    }

}
