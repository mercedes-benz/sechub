// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.NullSource;

class ScanGroupTest {

    @ParameterizedTest
    @EnumSource(ScanGroup.class)
    void resolveScanGroupOrNull_for_the_scan_types_from_group_group_is_resolved(ScanGroup group) {

        for (ScanType type : group.acceptedScanTypes) {
            ScanGroup foundGroup = ScanGroup.resolveScanGroupOrNull(Collections.singleton(type));
            if (!(group.equals(foundGroup))) {
                fail("Group:" + group + " should be resolved by scan type:" + type + " but resolved group was:" + foundGroup);
            }
        }
    }

    @Test
    void resolveScanGroupOrNull_for_null_returns_null() {
        assertNull(ScanGroup.resolveScanGroupOrNull(null));
    }

    @Test
    void resolveScanGroupOrNull_for_empty_set_returns_null() {
        assertNull(ScanGroup.resolveScanGroupOrNull(new LinkedHashSet<>()));
    }

    @Test
    void resolveScanGroupOrNull_for_report_returns_null() {
        assertNull(ScanGroup.resolveScanGroupOrNull(Collections.singleton(ScanType.REPORT)));
    }

    @Test
    void resolveScanGroupOrNull_for_unknown_returns_null() {
        assertNull(ScanGroup.resolveScanGroupOrNull(Collections.singleton(ScanType.UNKNOWN)));
    }

    @Test
    void resolveScanGroupOrNull_for_web_scan_returns_deployment_group() {
        assertEquals(ScanGroup.DEPLOYMENT, ScanGroup.resolveScanGroupOrNull(Collections.singleton(ScanType.WEB_SCAN)));
    }

    /**
     * It is forbidden that another group contains same scan type! We need the
     * uniqueness.
     */
    @Test
    void scan_type_is_assigned_only_for_one_group() {
        /* prepare */
        Map<ScanType, ScanGroup> map = new LinkedHashMap<>();

        /* test */
        for (ScanType scanType : ScanType.values()) {

            for (ScanGroup group : ScanGroup.values()) {
                if (group.isAssignedTo(scanType)) {
                    ScanGroup groupBefore = map.get(scanType);
                    if (groupBefore != null) {
                        fail("ScanType:" + scanType + " is already assigned to group:" + groupBefore + " so cannot be added also for group:" + group);
                    } else {
                        map.put(scanType, group);
                    }
                }

            }
        }
    }

    @Test
    void public_scan_types_are_all_assigned_to_groups() {
        /* prepare */
        List<ScanType> unassigned = new ArrayList<>(Arrays.asList(ScanType.values()));

        /* test */
        for (ScanType scanType : ScanType.values()) {
            if (scanType.isInternalScanType()) {
                // ignore pinternal parts here - we check only public
                unassigned.remove(scanType);
                continue;
            }
            for (ScanGroup group : ScanGroup.values()) {
                if (group.isAssignedTo(scanType)) {
                    unassigned.remove(scanType);
                }

            }
        }
        if (!unassigned.isEmpty()) {
            fail("Unassigned public scan types found:" + unassigned);
        }
    }

    @Test
    void internal_scan_types_are_NOT_assigned_to_any_group() {
        /* prepare */
        /* test */
        for (ScanType scanType : ScanType.values()) {
            if (!scanType.isInternalScanType()) {
                // ignore public parts here - we check only internal
                continue;
            }
            for (ScanGroup group : ScanGroup.values()) {
                if (group.isAssignedTo(scanType)) {
                    fail("Scan type: " + scanType + " is internal and may not be inside a group!");
                }

            }
        }
    }

    @EnumSource(mode = Mode.INCLUDE, value = ScanType.class, names = { "CODE_SCAN", "LICENSE_SCAN" })
    @ParameterizedTest
    void group_content_accepts(ScanType scanType) {
        assertGroupAssignedTo(ScanGroup.CONTENT, scanType);
    }

    @EnumSource(mode = Mode.EXCLUDE, value = ScanType.class, names = { "CODE_SCAN", "LICENSE_SCAN" })
    @ParameterizedTest
    @NullSource
    void group_content_NOT_accepts(ScanType scanType) {
        assertGroupNOTAssignedTo(ScanGroup.CONTENT, scanType);
    }

    @EnumSource(mode = Mode.INCLUDE, value = ScanType.class, names = { "WEB_SCAN" })
    @ParameterizedTest
    void group_deployment_accepts(ScanType scanType) {
        assertGroupAssignedTo(ScanGroup.DEPLOYMENT, scanType);
    }

    @EnumSource(mode = Mode.EXCLUDE, value = ScanType.class, names = { "WEB_SCAN" })
    @ParameterizedTest
    @NullSource
    void group_deployment_NOT_accepts(ScanType scanType) {
        assertGroupNOTAssignedTo(ScanGroup.DEPLOYMENT, scanType);
    }

    @EnumSource(mode = Mode.INCLUDE, value = ScanType.class, names = { "INFRA_SCAN" })
    @ParameterizedTest
    void group_network_accepts(ScanType scanType) {
        assertGroupAssignedTo(ScanGroup.NETWORK, scanType);
    }

    @EnumSource(mode = Mode.EXCLUDE, value = ScanType.class, names = { "INFRA_SCAN" })
    @ParameterizedTest
    @NullSource
    void group_network_NOT_accepts(ScanType scanType) {
        assertGroupNOTAssignedTo(ScanGroup.NETWORK, scanType);
    }

    private void assertGroupAssignedTo(ScanGroup content, ScanType scanType) {
        assertGroupAssignment(content, scanType, true);
    }

    private void assertGroupNOTAssignedTo(ScanGroup content, ScanType scanType) {
        assertGroupAssignment(content, scanType, false);
    }

    private void assertGroupAssignment(ScanGroup content, ScanType scanType, boolean expectedAcceptance) {
        assertEquals(expectedAcceptance, content.isAssignedTo(scanType));
    }

}
