// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import java.util.Optional;

public class UploadDefinition extends AbstractDefinition {

    private Optional<String> binariesFolder = Optional.ofNullable(null);
    private Optional<String> sourceFolder = Optional.ofNullable(null);
    private Optional<String> referenceId = Optional.ofNullable(null);

    private Optional<String> binaryFile = Optional.ofNullable(null);
    private Optional<String> sourceFile = Optional.ofNullable(null);

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

    public Optional<String> getBinaryFile() {
        return binaryFile;
    }

    public void setBinaryFile(Optional<String> binaryFile) {
        this.binaryFile = binaryFile;
    }

    public Optional<String> getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(Optional<String> sourceFile) {
        this.sourceFile = sourceFile;
    }

}
