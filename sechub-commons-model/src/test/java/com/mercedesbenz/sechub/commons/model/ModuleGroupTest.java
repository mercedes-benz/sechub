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

class ModuleGroupTest {

    @ParameterizedTest
    @EnumSource(ModuleGroup.class)
    void resolveModuleGroupOrNull_for_the_scan_types_from_group_group_is_resolved(ModuleGroup groupToTest) {

        for (ScanType type : groupToTest.moduleScanTypes) {
            ModuleGroup resolvedGroup = ModuleGroup.resolveModuleGroupOrNull(Collections.singleton(type));
            if (!(groupToTest.equals(resolvedGroup))) {
                fail("Group:" + groupToTest + " should be resolved by scan type:" + type + " but resolved group was:" + resolvedGroup);
            }
        }
    }

    @Test
    void resolveModuleGroupOrNull_for_null_returns_null() {
        assertNull(ModuleGroup.resolveModuleGroupOrNull(null));
    }

    @Test
    void resolveModuleGroupOrNull_for_empty_set_returns_null() {
        assertNull(ModuleGroup.resolveModuleGroupOrNull(new LinkedHashSet<>()));
    }

    @Test
    void resolveModuleGroupOrNull_for_report_returns_null() {
        assertNull(ModuleGroup.resolveModuleGroupOrNull(Collections.singleton(ScanType.REPORT)));
    }

    @Test
    void resolveModuleGroupOrNull_for_unknown_returns_null() {
        assertNull(ModuleGroup.resolveModuleGroupOrNull(Collections.singleton(ScanType.UNKNOWN)));
    }

    @Test
    void resolveModuleGroupOrNull_for_web_scan_returns_dynamic_group() {
        assertEquals(ModuleGroup.DYNAMIC, ModuleGroup.resolveModuleGroupOrNull(Collections.singleton(ScanType.WEB_SCAN)));
    }

    /**
     * It is forbidden that another group contains same scan type! We need the
     * uniqueness.
     */
    @Test
    void scan_type_is_assigned_only_for_one_group() {
        /* prepare */
        Map<ScanType, ModuleGroup> map = new LinkedHashMap<>();

        /* test */
        for (ScanType scanType : ScanType.values()) {

            for (ModuleGroup group : ModuleGroup.values()) {
                if (group.isGivenModuleInGroup(scanType)) {
                    ModuleGroup groupBefore = map.get(scanType);
                    if (groupBefore != null) {
                        fail("ScanType:" + scanType + " is already assigned to group:" + groupBefore + " so cannot be added also to group:" + group);
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
                // ignore internal parts here - we check only public
                unassigned.remove(scanType);
                continue;
            }
            for (ModuleGroup group : ModuleGroup.values()) {
                if (group.isGivenModuleInGroup(scanType)) {
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
        /* test */
        for (ScanType scanType : ScanType.values()) {
            if (!scanType.isInternalScanType()) {
                // ignore public parts here - we check only internal
                continue;
            }
            for (ModuleGroup group : ModuleGroup.values()) {
                if (group.isGivenModuleInGroup(scanType)) {
                    fail("Scan type: " + scanType + " is internal and may not be inside a group!");
                }

            }
        }
    }

    @EnumSource(mode = Mode.INCLUDE, value = ScanType.class, names = { "CODE_SCAN", "LICENSE_SCAN", "SECRET_SCAN", "IAC_SCAN" })
    @ParameterizedTest
    void group_static_accepts(ScanType scanType) {
        assertGroupAssignedTo(ModuleGroup.STATIC, scanType);
    }

    @EnumSource(mode = Mode.EXCLUDE, value = ScanType.class, names = { "CODE_SCAN", "LICENSE_SCAN", "SECRET_SCAN", "IAC_SCAN" })
    @ParameterizedTest
    @NullSource
    void group_static_NOT_accepts(ScanType scanType) {
        assertGroupNOTAssignedTo(ModuleGroup.STATIC, scanType);
    }

    @EnumSource(mode = Mode.INCLUDE, value = ScanType.class, names = { "WEB_SCAN" })
    @ParameterizedTest
    void group_dynamic_accepts(ScanType scanType) {
        assertGroupAssignedTo(ModuleGroup.DYNAMIC, scanType);
    }

    @EnumSource(mode = Mode.EXCLUDE, value = ScanType.class, names = { "WEB_SCAN" })
    @ParameterizedTest
    @NullSource
    void group_dynamic_NOT_accepts(ScanType scanType) {
        assertGroupNOTAssignedTo(ModuleGroup.DYNAMIC, scanType);
    }

    @EnumSource(mode = Mode.INCLUDE, value = ScanType.class, names = { "INFRA_SCAN" })
    @ParameterizedTest
    void group_network_accepts(ScanType scanType) {
        assertGroupAssignedTo(ModuleGroup.NETWORK, scanType);
    }

    @EnumSource(mode = Mode.EXCLUDE, value = ScanType.class, names = { "INFRA_SCAN" })
    @ParameterizedTest
    @NullSource
    void group_network_NOT_accepts(ScanType scanType) {
        assertGroupNOTAssignedTo(ModuleGroup.NETWORK, scanType);
    }

    private void assertGroupAssignedTo(ModuleGroup group, ScanType scanType) {
        assertGroupAssignment(group, scanType, true);
    }

    private void assertGroupNOTAssignedTo(ModuleGroup group, ScanType scanType) {
        assertGroupAssignment(group, scanType, false);
    }

    private void assertGroupAssignment(ModuleGroup group, ScanType scanType, boolean expectedAcceptance) {
        assertEquals(expectedAcceptance, group.isGivenModuleInGroup(scanType));
    }

}
