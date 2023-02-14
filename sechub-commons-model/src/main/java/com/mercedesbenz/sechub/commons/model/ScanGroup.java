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
 * A group represents a set of scan types. A scan type is always assigned only
 * to ONE group! An internal scan type may not be part of a group!
 *
 * @author Albert Tregnaghi
 *
 */
@MustBeKeptStable("Scan group is used inside DB entries. Do NOT remove something or change name of enum values!")
public enum ScanGroup {

    CONTENT("content", CODE_SCAN, LICENSE_SCAN),

    DEPLOYMENT("deployment", WEB_SCAN),

    NETWORK("network", INFRA_SCAN),

    ;

    private static final Logger LOG = LoggerFactory.getLogger(ScanGroup.class);

    static Map<ScanType, ScanGroup> scanTypeToGroupMap = new HashMap<>();

    static {
        initializeMappingAndValidate();
    }

    /*
     * Initialize mapping + ensure one scan type is defined exactly for one scan
     * group only and it contains no internal scan types!
     *
     * We use the map in addition to normal fields, so we have here a little
     * redundancy, but this ensures, that the mapping is really unique (otherwise we
     * have errors already at class loading time). Also the scan group resolution
     * should be faster (we use the (hash)map here as well)
     */
    private static void initializeMappingAndValidate() {
        for (ScanType scanType : ScanType.values()) {

            for (ScanGroup groupToAssign : ScanGroup.values()) {
                if (groupToAssign.name().length() > 20) {
                    throw new IllegalStateException("Name may only have a length of 20 chars");
                }
                if (groupToAssign.isAccepting(scanType)) {
                    if (scanType.isInternalScanType()) {
                        throw new IllegalStateException("The scan type: " + scanType + " is internal and was assigned to scan group: " + groupToAssign
                                + ". Internal scan types may not be assigned to groups!");
                    }
                    ScanGroup allreadyAssignedGroup = scanTypeToGroupMap.get(scanType);
                    if (allreadyAssignedGroup != null) {
                        throw new IllegalStateException("The scan type: " + scanType + " is already assigned to scan group: " + allreadyAssignedGroup
                                + ", so it cannot be added also for group:" + groupToAssign);
                    } else {
                        scanTypeToGroupMap.put(scanType, groupToAssign);
                    }
                }

            }
        }
    }

    private String id;
    final ScanType[] acceptedScanTypes;

    private ScanGroup(String id, ScanType... acceptedScanTypes) {
        this.id = id;
        this.acceptedScanTypes = acceptedScanTypes;
    }

    @JsonValue
    public String getId() {
        return id;
    }

    /**
     * Resolves the scan group for a set of scan types. The given types must be all
     * parts of a group. If types are not assigned to a group or the types belong to
     * multiple groups the result will be <code>null</code> because no explicit
     * group can be resolved.
     *
     * @param types
     * @return group or <code>null</code> when group is not clear/found.
     */
    public static ScanGroup resolveScanGroupOrNull(Set<ScanType> types) {
        if (types == null || types.isEmpty()) {
            return null;
        }
        ScanGroup groupFound = null;

        for (ScanType type : types) {

            ScanGroup found = scanTypeToGroupMap.get(type);
            if (found == null) {
                return null;
            }
            if (groupFound == null) {
                groupFound = found;
            } else {
                if (!groupFound.equals(found)) {
                    LOG.debug("Cannot resolve scan group - scan types: {} would lead to at least two scan groups: {}, {}.", types, groupFound, found);
                    return null;
                }
            }
        }

        return groupFound;
    }

    public boolean isAssignedTo(ScanType scanType) {
        ScanGroup group = scanTypeToGroupMap.get(scanType);
        return this.equals(group);
    }

    private boolean isAccepting(ScanType scanType) {
        if (scanType == null) {
            return false;
        }
        for (ScanType accepted : acceptedScanTypes) {
            if (accepted.equals(scanType)) {
                return true;
            }
        }
        return false;
    }
}
