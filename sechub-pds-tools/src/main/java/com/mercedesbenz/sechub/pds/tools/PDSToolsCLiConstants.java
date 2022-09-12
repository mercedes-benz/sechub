// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.ScanType;

public class PDSToolsCLiConstants {

    public static final String CMD_HELP = "--help";
    public static final String CMD_GENERATE = "--generate";

    public static final List<ScanType> NO_REPORT_OR_UNKNOWN = Collections.unmodifiableList(createAcceptedScanTypes());

    private static List<ScanType> createAcceptedScanTypes() {
        List<ScanType> acceptedScanTypes = new ArrayList<>();

        for (ScanType type : ScanType.values()) {
            switch (type) {
            case REPORT:
            case UNKNOWN:
                // we do not accept those types
                continue;
            default:
                acceptedScanTypes.add(type);

            }
        }
        return acceptedScanTypes;

    }
}
