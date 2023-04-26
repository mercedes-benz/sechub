package com.mercedesbenz.sechub.systemtest.config;

import java.util.Optional;

public class UploadDefinition extends AbstractDefinition {

    private Optional<String> binariesFolder;
    private Optional<String> sourceFolder;
    private Optional<String> referenceId;

    public Optional<String> getBinariesFolder() {
        return binariesFolder;
    }

    public void setBinariesFolder(Optional<String> binariesFolder) {
        this.binariesFolder = binariesFolder;
    }

    public Optional<String> getSourceFolder() {
        return sourceFolder;
    }

    public void setSourceFolder(Optional<String> sourceFolder) {
        this.sourceFolder = sourceFolder;
    }

    public Optional<String> getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Optional<String> referenceId) {
        this.referenceId = referenceId;
    }
}
