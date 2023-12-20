// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.config;

import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperScanTypes;

public class XrayWrapperArtifact {

    private String name;

    private String checksum;

    private String tag;

    private XrayWrapperScanTypes artifactType;

    public XrayWrapperArtifact(String name, String checksum, String tag, XrayWrapperScanTypes artifactType) {
        this.name = name;
        this.checksum = checksum;
        this.tag = tag;
        this.artifactType = artifactType;
    }

    public String getName() {
        return name;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getTag() {
        return tag;
    }

    public XrayWrapperScanTypes getArtifactType() {
        return artifactType;
    }
}
