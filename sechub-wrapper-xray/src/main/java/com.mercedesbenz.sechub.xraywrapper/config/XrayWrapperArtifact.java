package com.mercedesbenz.sechub.xraywrapper.config;

import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperScanTypes;

public class XrayWrapperArtifact {

    private String name;

    private String secureHash;

    private String tag;

    private XrayWrapperScanTypes artifactType;

    public XrayWrapperArtifact(String name, String sha256, String tag, XrayWrapperScanTypes artifactType) {
        this.name = name;
        this.secureHash = sha256;
        this.tag = tag;
        this.artifactType = artifactType;
    }

    public String getName() {
        return name;
    }

    public String getSecureHash() {
        return secureHash;
    }

    public String getTag() {
        return tag;
    }

    public XrayWrapperScanTypes getArtifactType() {
        return artifactType;
    }
}
