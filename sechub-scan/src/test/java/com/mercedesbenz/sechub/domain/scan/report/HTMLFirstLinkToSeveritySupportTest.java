package com.mercedesbenz.sechub.domain.scan.report;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.Severity;

class HTMLFirstLinkToSeveritySupportTest {

    private HTMLFirstLinkToSeveritySupport helperToTest;

    @BeforeEach
    void beforeEach() {
        helperToTest = new HTMLFirstLinkToSeveritySupport();
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void createAnkerFirstOf_not_null_not_empty_and_starts_with_character(ScanType scanType) {
        for (Severity severity : Severity.values()) {

            String ankerName = helperToTest.createAnkerFirstOf(scanType, severity);

            assertNotNull(ankerName);

            assertFalse(ankerName.isEmpty());
            assertFalse(ankerName.startsWith("#"));
            assertTrue(ankerName.length() > 5);
            assertTrue(Character.isAlphabetic(ankerName.charAt(0)));
        }
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void createLinkToFirstOf_works_with_anker(ScanType scanType) {
        for (Severity severity : Severity.values()) {
            assertEquals("#" + helperToTest.createAnkerFirstOf(scanType, severity), helperToTest.createLinkToFirstOf(scanType, severity));
        }
    }
}
