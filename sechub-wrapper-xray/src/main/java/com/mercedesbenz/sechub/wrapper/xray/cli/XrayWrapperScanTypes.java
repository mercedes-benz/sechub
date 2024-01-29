// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.cli;

import java.util.Arrays;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;

public enum XrayWrapperScanTypes {
    DOCKER("docker"),;

    private String type;

    XrayWrapperScanTypes(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public static XrayWrapperScanTypes fromString(String type) throws XrayWrapperException {
        if (type == null) {
            throw new XrayWrapperCommandLineParserException("Scan status is NULL");
        }
        for (XrayWrapperScanTypes scanType : XrayWrapperScanTypes.values()) {
            if (scanType.type.equals(type)) {
                return scanType;
            }
        }
        throw new XrayWrapperCommandLineParserException(
                "Scan type not supported: " + type + "\n supported types are: " + Arrays.toString(XrayWrapperScanTypes.values()));
    }
}