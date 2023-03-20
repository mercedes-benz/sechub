package com.mercedesbenz.sechub.systemtest.config;

public class UploadDefinition extends AbstractDefinition {

    private String binariesFolder;
    private String sourceFolder;

    public String getBinariesFolder() {
        return binariesFolder;
    }

    public void setBinariesFolder(String binaries) {
        this.binariesFolder = binaries;
    }

    public void setSourceFolder(String sources) {
        this.sourceFolder = sources;
    }

    public String getSourceFolder() {
        return sourceFolder;
    }
}
