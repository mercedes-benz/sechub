// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.metadata;

import static org.junit.Assert.*;

import org.junit.Test;

public class SerecoSeverityTest {

    @Test
    public void value_null_is_null() {
        assertNull(SerecoSeverity.fromString(null));
    }

    @Test
    public void value_unknown_value12345_is_null() {
        assertNull(SerecoSeverity.fromString("unknown_value12345"));
    }

    @Test
    public void value_as_uppercase_is_returned_as_severity() {
        for (SerecoSeverity severity : SerecoSeverity.values()) {
            String name = severity.name();

            assertEquals(severity, SerecoSeverity.fromString(name.toUpperCase()));
        }
    }

    @Test
    public void value_as_lowercase_is_returned_as_severity() {
        for (SerecoSeverity severity : SerecoSeverity.values()) {
            String name = severity.name();

            assertEquals(severity, SerecoSeverity.fromString(name.toLowerCase()));
        }
    }

    @Test
    public void value_as_firstUpperCasedThanLowerCase_is_returned_as_severity() {
        for (SerecoSeverity severity : SerecoSeverity.values()) {
            String name = severity.name();
            String lowerCase = name.toLowerCase();
            String upperCase = name.toUpperCase();
            String firstUpperCasedThanLowerCase = upperCase.substring(0, 1) + lowerCase.substring(1);

            assertEquals(severity, SerecoSeverity.fromString(firstUpperCasedThanLowerCase));

        }
    }

}
