package com.mercedesbenz.sechub.xraywrapper.config;

public class XrayWrapperArtifact {

    private String name;

    private String sha256;

    private String tag;

    private String artifactType;

    public XrayWrapperArtifact(String name, String sha256, String tag, String artifactType) {
        this.name = name;
        this.sha256 = sha256;
        this.tag = tag;
        this.artifactType = artifactType;
    }

    public String getName() {
        return name;
    }

    public String getSha256() {
        return sha256;
    }

    public String getTag() {
        return tag;
    }

    public String getArtifactType() {
        return artifactType;
    }
}
