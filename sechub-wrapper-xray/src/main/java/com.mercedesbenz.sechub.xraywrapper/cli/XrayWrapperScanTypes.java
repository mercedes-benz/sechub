package com.mercedesbenz.sechub.xraywrapper.cli;

import java.util.Arrays;

public enum XrayWrapperScanTypes {
    DOCKER("docker"),;

    private String type;

    XrayWrapperScanTypes(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public static XrayWrapperScanTypes fromString(String type) {
        for (XrayWrapperScanTypes scanType : XrayWrapperScanTypes.values()) {
            if (scanType.type.equals(type)) {
                return scanType;
            }
        }
        throw new XrayWrapperCommandLineParserException(
                "Scan type not supported: " + type + "\n supported types are: " + Arrays.toString(XrayWrapperScanTypes.values()));
    }
}