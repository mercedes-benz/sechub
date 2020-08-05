package com.daimler.sechub.client.java.report;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SeverityTest {

    @Test
    void toString_test() {
        Severity severity = Severity.CRITICAL;

        assertEquals("CRITICAL", severity.toString());
    }

    @Test
    void fromString_test() {
        String severityCritical = "CRITICAL";

        Severity actualSeverity = Severity.valueOf(severityCritical);

        assertEquals(Severity.CRITICAL, actualSeverity);
    }

    @Test
    void compareTo_critical_to_info() {
        Severity critical = Severity.CRITICAL;
        Severity info = Severity.INFO;

        assertTrue(info.compareTo(critical) < 0);
    }

    @Test
    void compareTo_info_to_critical() {
        Severity critical = Severity.CRITICAL;
        Severity info = Severity.INFO;

        assertTrue(critical.compareTo(info) > 0);
    }

    @Test
    void compareTo_both_medium() {
        Severity medium1 = Severity.MEDIUM;
        Severity medium2 = Severity.MEDIUM;

        assertEquals(0, medium1.compareTo(medium2));
    }
}
