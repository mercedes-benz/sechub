// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

class SecHubFindingTest {

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void hasScanType(ScanType scanTypeToCheck) {
        /* prepare */
        SecHubFinding finding = new SecHubFinding();
        finding.setType(scanTypeToCheck);

        /* test */
        assertTrue(finding.hasScanType(scanTypeToCheck));

        for (ScanType scanType : ScanType.values()) {
            if (!scanType.equals(scanTypeToCheck)) {
                assertFalse(finding.hasScanType(scanType));
            }
        }

    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void hasScanTypeId(ScanType scanTypeToCheck) {
        /* prepare */
        SecHubFinding finding = new SecHubFinding();
        finding.setType(scanTypeToCheck);

        /* test */
        assertTrue(finding.hasScanType(scanTypeToCheck.getId()));

        for (ScanType scanType : ScanType.values()) {
            if (!scanType.equals(scanTypeToCheck)) {
                assertFalse(finding.hasScanType(scanType.getId()));
            }
        }
    }

    @Test
    void hasScanType_null() {
        /* prepare */
        SecHubFinding finding = new SecHubFinding();
        finding.setType(null);

        /* test */
        assertTrue(finding.hasScanType((ScanType) null));

        for (ScanType scanType : ScanType.values()) {
            assertFalse(finding.hasScanType(scanType));
        }
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void hasScanTypeId_empty_or_null_when_finding_has_no_type(String scanTypeId) {
        /* prepare */
        SecHubFinding finding = new SecHubFinding();
        finding.setType(null);

        /* test */
        assertTrue(finding.hasScanType(scanTypeId));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void hasScanTypeId_empty_or_null_when_finding_has_type(String scanTypeId) {
        /* prepare */
        SecHubFinding finding = new SecHubFinding();
        finding.setType(ScanType.CODE_SCAN);

        /* test */
        assertFalse(finding.hasScanType(scanTypeId));
    }

    @Test
    void compare_to__an_array_list_containing_findings_can_be_sorted_by_collections_for_severity() {
        /* prepare */
        SecHubFinding finding1Medium = new SecHubFinding();
        finding1Medium.severity = Severity.MEDIUM;

        SecHubFinding finding2Low = new SecHubFinding();
        finding2Low.severity = Severity.LOW;

        SecHubFinding finding3Critical = new SecHubFinding();
        finding3Critical.severity = Severity.CRITICAL;

        List<SecHubFinding> list = new ArrayList<>();
        list.add(finding1Medium);
        list.add(finding2Low);
        list.add(finding3Critical);
        /* execute */
        Collections.sort(list);

        /* test */
        Iterator<SecHubFinding> it = list.iterator();
        assertEquals(finding3Critical, it.next());
        assertEquals(finding1Medium, it.next());
        assertEquals(finding2Low, it.next());

    }

    @Test
    void compare_to__an_array_list_containing_findings_can_be_sorted_by_collections_for_cweId() {
        /* prepare */
        SecHubFinding finding1_cweId10 = new SecHubFinding();
        finding1_cweId10.cweId = 10;

        SecHubFinding finding2_cweId5 = new SecHubFinding();
        finding2_cweId5.cweId = 5;

        SecHubFinding finding3_cweId30 = new SecHubFinding();
        finding3_cweId30.cweId = 30;

        List<SecHubFinding> list = new ArrayList<>();
        list.add(finding1_cweId10);
        list.add(finding2_cweId5);
        list.add(finding3_cweId30);

        /* execute */
        Collections.sort(list);

        /* test */
        Iterator<SecHubFinding> it = list.iterator();
        assertEquals(finding2_cweId5, it.next());
        assertEquals(finding1_cweId10, it.next());
        assertEquals(finding3_cweId30, it.next());

    }

    @Test
    void compare_to__an_array_list_containing_findings_can_be_sorted_by_collections_for_cveId() {
        /* prepare */
        SecHubFinding finding1_cveIdabc = new SecHubFinding();
        finding1_cveIdabc.cveId = "abc";

        SecHubFinding finding2_cveIxyz = new SecHubFinding();
        finding2_cveIxyz.cveId = "xyz";

        SecHubFinding finding3_cveIdab = new SecHubFinding();
        finding3_cveIdab.cveId = "ab";

        List<SecHubFinding> list = new ArrayList<>();
        list.add(finding1_cveIdabc);
        list.add(finding2_cveIxyz);
        list.add(finding3_cveIdab);
        /* execute */
        Collections.sort(list);

        /* test */
        Iterator<SecHubFinding> it = list.iterator();
        assertEquals(finding3_cveIdab, it.next());
        assertEquals(finding1_cveIdabc, it.next());
        assertEquals(finding2_cveIxyz, it.next());

    }

    @Test
    void compare_to__an_array_list_containing_findings_can_be_sorted_by_collections_for_severity_cwe_id_cveId() {
        /* prepare */
        SecHubFinding finding1_mediumCwe1Cve3 = new SecHubFinding();
        finding1_mediumCwe1Cve3.cveId = "3";
        finding1_mediumCwe1Cve3.cweId = 1;
        finding1_mediumCwe1Cve3.severity = Severity.MEDIUM;

        SecHubFinding finding2_criticalCwe2Cve2 = new SecHubFinding();
        finding2_criticalCwe2Cve2.cveId = "2";
        finding2_criticalCwe2Cve2.cweId = 2;
        finding2_criticalCwe2Cve2.severity = Severity.CRITICAL;

        SecHubFinding finding3_mediumCwe2Cve1 = new SecHubFinding();
        finding3_mediumCwe2Cve1.cveId = "1";
        finding3_mediumCwe2Cve1.cweId = 2;
        finding3_mediumCwe2Cve1.severity = Severity.MEDIUM;

        SecHubFinding finding4_nothing_set = new SecHubFinding();

        SecHubFinding finding5_medium_nothing_else_set = new SecHubFinding();
        finding5_medium_nothing_else_set.severity = Severity.MEDIUM;

        SecHubFinding finding6_lowCwe2Cve1 = new SecHubFinding();
        finding6_lowCwe2Cve1.cveId = "1";
        finding6_lowCwe2Cve1.cweId = 2;
        finding6_lowCwe2Cve1.severity = Severity.LOW;

        SecHubFinding finding7_lowCve1_no_cweId = new SecHubFinding();
        finding7_lowCve1_no_cweId.severity = Severity.LOW;
        finding7_lowCve1_no_cweId.cveId = "1";

        SecHubFinding finding8_lowCwe1_no_cveId = new SecHubFinding();
        finding8_lowCwe1_no_cveId.severity = Severity.LOW;
        finding8_lowCwe1_no_cveId.cweId = 1;

        SecHubFinding finding9_noSeverity_cwe1_no_cveId = new SecHubFinding();
        finding9_noSeverity_cwe1_no_cveId.cweId = 1;

        SecHubFinding finding10_noSeverity_cve1_no_cweId = new SecHubFinding();
        finding10_noSeverity_cve1_no_cweId.cveId = "1";

        List<SecHubFinding> list = new ArrayList<>();
        list.add(finding1_mediumCwe1Cve3);
        list.add(finding2_criticalCwe2Cve2);
        list.add(finding3_mediumCwe2Cve1);
        list.add(finding4_nothing_set);
        list.add(finding5_medium_nothing_else_set);
        list.add(finding6_lowCwe2Cve1);
        list.add(finding7_lowCve1_no_cweId);
        list.add(finding8_lowCwe1_no_cveId);
        list.add(finding9_noSeverity_cwe1_no_cveId);
        list.add(finding10_noSeverity_cve1_no_cweId);

        /* execute */
        Collections.sort(list);

        /* test */
        Iterator<SecHubFinding> it = list.iterator();
        assertEquals(finding2_criticalCwe2Cve2, it.next());

        assertEquals(finding1_mediumCwe1Cve3, it.next());
        assertEquals(finding3_mediumCwe2Cve1, it.next());
        assertEquals(finding5_medium_nothing_else_set, it.next());

        assertEquals(finding8_lowCwe1_no_cveId, it.next());
        assertEquals(finding6_lowCwe2Cve1, it.next());

        assertEquals(finding7_lowCve1_no_cweId, it.next());

        assertEquals(finding9_noSeverity_cwe1_no_cveId, it.next());
        assertEquals(finding10_noSeverity_cve1_no_cweId, it.next());
        assertEquals(finding4_nothing_set, it.next());

    }

    @Test
    void initial_finding_has_scan_type_null() {
        /* prepare */
        SecHubFinding finding = new SecHubFinding();

        /* test */
        assertNull(finding.getType());

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
