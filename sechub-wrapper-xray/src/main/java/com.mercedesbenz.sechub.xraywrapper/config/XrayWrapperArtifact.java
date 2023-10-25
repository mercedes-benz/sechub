package com.mercedesbenz.sechub.xraywrapper.config;

import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperExitCode;
import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperRuntimeException;
import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperScanTypes;

public class XrayWrapperArtifact {

    private String name;

    private String checksum;

    private String tag;

    private XrayWrapperScanTypes artifactType;

    public XrayWrapperArtifact(String name, String checksum, String tag, XrayWrapperScanTypes artifactType) {
        if (artifactType == null) {
            throw new XrayWrapperRuntimeException("Artifact scan type cannot be null!", XrayWrapperExitCode.NOT_NULLABLE);
        }
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
