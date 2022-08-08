// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

public class WorkspaceLocationData {
    String workspaceLocation;
    String resultFileLocation;
    String extractedSourcesLocation;
    String sourceCodeZipFileLocation;
    String extractedBinariesLocation;
    String binariesTarFileLocation;
    String userMessagesLocation;
    String metaDataFileLocation;

    public String getWorkspaceLocation() {
        return workspaceLocation;
    }

    public String getResultFileLocation() {
        return resultFileLocation;
    }

    public String getExtractedSourcesLocation() {
        return extractedSourcesLocation;
    }

    public String getSourceCodeZipFileLocation() {
        return sourceCodeZipFileLocation;
    }

    public String getExtractedBinariesLocation() {
        return extractedBinariesLocation;
    }

    public String getBinariesTarFileLocation() {
        return binariesTarFileLocation;
    }

    public String getUserMessagesLocation() {
        return userMessagesLocation;
    }

    public String getMetaDataFileLocation() {
        return metaDataFileLocation;
    }
}
