// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static com.mercedesbenz.sechub.commons.model.ScanType.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonValue;
import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;

/**
 * A module group represents a set of modules. Modules are security products of
 * same scan types. We use the scan type to map modules to module groups. A scan
 * type is always assigned only to ONE group! An internal scan type represents
 * no module and may not be part of a group!
 *
 * @author Albert Tregnaghi
 *
 */
@MustBeKeptStable("Module group is used inside DB entries. Do NOT remove something or change name of enum values!")
public enum ModuleGroup {

    STATIC("static", CODE_SCAN, LICENSE_SCAN, SECRET_SCAN, IAC_SCAN),

    DYNAMIC("dynamic", WEB_SCAN),

    NETWORK("network", INFRA_SCAN),

    ;

    private static final Logger LOG = LoggerFactory.getLogger(ModuleGroup.class);

    static Map<ScanType, ModuleGroup> scanTypeToModuleGroupMap = new HashMap<>();

    static {
        initializeMappingAndValidate();
    }

    /*
     * Initialize mapping + ensure one scan type is defined exactly for one scan
     * group only and it contains no internal scan types!
     *
     * We use the map in addition to normal fields, so we have here a little
     * redundancy, but this ensures, that the mapping is really unique (otherwise we
     * have errors already at class loading time). Also the module group resolution
     * should be faster (we use the (hash)map here as well)
     */
    private static void initializeMappingAndValidate() {
        for (ScanType scanType : ScanType.values()) {

            for (ModuleGroup groupToAssign : ModuleGroup.values()) {
                if (groupToAssign.name().length() > 20) {
                    throw new IllegalStateException("Name may only have a length of 20 chars");
                }
                boolean found = false;
                for (ScanType accepted : groupToAssign.moduleScanTypes) {
                    if (accepted.equals(scanType)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    if (scanType.isInternalScanType()) {
                        throw new IllegalStateException("The scan type: " + scanType + " is internal and was assigned to module group: " + groupToAssign
                                + ". Internal scan types may not be assigned to groups!");
                    }
                    ModuleGroup allreadyAssignedGroup = scanTypeToModuleGroupMap.get(scanType);
                    if (allreadyAssignedGroup != null) {
                        throw new IllegalStateException("The scan type: " + scanType + " is already assigned to module group: " + allreadyAssignedGroup
                                + ", so it cannot be added also to group:" + groupToAssign);
                    } else {
                        scanTypeToModuleGroupMap.put(scanType, groupToAssign);
                    }
                }

            }
        }
    }

    private String id;
    final ScanType[] moduleScanTypes;

    private ModuleGroup(String id) {
        throw new IllegalStateException("At least one scan type must be defined for a module group!");
    }

    private ModuleGroup(String id, ScanType... moduleScanTypes) {
        this.id = id;
        this.moduleScanTypes = moduleScanTypes;
    }

    public ScanType[] getModuleScanTypes() {
        return moduleScanTypes;
    }

    @JsonValue
    public String getId() {
        return id;
    }

    /**
     * Resolves the module group for a given set of scan types. The given types must
     * be all represented by one group. If types are not assigned to a group or the
     * types belong to multiple groups the result will be <code>null</code> because
     * no explicit group can be resolved.
     *
     * @param types
     * @return group or <code>null</code> when group is not clear/found.
     */
    public static ModuleGroup resolveModuleGroupOrNull(Set<ScanType> types) {
        if (types == null || types.isEmpty()) {
            return null;
        }
        ModuleGroup groupFound = null;

        for (ScanType type : types) {

            ModuleGroup found = scanTypeToModuleGroupMap.get(type);
            if (found == null) {
                return null;
            }
            if (groupFound == null) {
                groupFound = found;
            } else {
                if (!groupFound.equals(found)) {
                    LOG.debug("Cannot resolve module group - modules having scan types: {} would lead to at least two module groups: {}, {}.", types,
                            groupFound, found);
                    return null;
                }
            }
        }

        return groupFound;
    }

    public boolean isGivenModuleInGroup(ScanType moduleScanType) {
        ModuleGroup group = scanTypeToModuleGroupMap.get(moduleScanType);
        return this.equals(group);
    }

}
