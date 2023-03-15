// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

class SecHubFindingTest {

    @Test
    void initial_finding_has_scan_type_null() {
        /* prepare */
        SecHubFinding finding = new SecHubFinding();

        /* test */
        assertNull(finding.getType());
        assertFalse(finding.hasScanType(null));

        for (ScanType otherScanType : ScanType.values()) {
            assertHasScanTypeReturnsFalseForAnyVariantOf(finding, otherScanType.getId());
        }
    }

    @ParameterizedTest
    @EnumSource(value = ScanType.class)
    void finding_has_scantype_returns_true_for_set_scan_type(ScanType scanTypeToTest) {
        /* prepare */
        SecHubFinding finding = new SecHubFinding();
        finding.setType(scanTypeToTest);

        /* test */
        String scanTypeId = scanTypeToTest.getId();
        assertTrue(finding.hasScanType(scanTypeId));
        assertTrue(finding.hasScanType(scanTypeId.toUpperCase()));
        assertTrue(finding.hasScanType(scanTypeId.toLowerCase()));
        assertTrue(finding.hasScanType(createStringFirstCharUpperCasedOtherLowerCased(scanTypeId)));

    }

    @ParameterizedTest
    @EnumSource(value = ScanType.class)
    @NullSource
    void finding_has_scantype_returns_false_for_other_than_set_scan_type(ScanType scanTypeToTest) {
        /* prepare */
        SecHubFinding finding = new SecHubFinding();
        finding.setType(scanTypeToTest);

        /* test */
        for (ScanType otherScanType : ScanType.values()) {
            if (scanTypeToTest == otherScanType) {
                // we ignore same type - we test against others!
                continue;
            }
            assertHasScanTypeReturnsFalseForAnyVariantOf(finding, otherScanType.getId());
        }
    }

    private void assertHasScanTypeReturnsFalseForAnyVariantOf(SecHubFinding finding, String otherScanTypeId) {
        assertFalse(finding.hasScanType(otherScanTypeId));
        assertFalse(finding.hasScanType(otherScanTypeId.toUpperCase()));
        assertFalse(finding.hasScanType(otherScanTypeId.toLowerCase()));
        assertFalse(finding.hasScanType(createStringFirstCharUpperCasedOtherLowerCased(otherScanTypeId)));
    }

    private String createStringFirstCharUpperCasedOtherLowerCased(String scanTypeId) {
        return ("" + scanTypeId.charAt(0)).toUpperCase() + scanTypeId.substring(1).toLowerCase();
    }

}
