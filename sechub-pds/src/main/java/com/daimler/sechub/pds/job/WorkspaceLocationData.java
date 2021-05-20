// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

public class WorkspaceLocationData {
    String workspaceLocation;
    String resultFileLocation;
    String unzippedSourceLocation;
    String zippedSourceLocation;

    public String getWorkspaceLocation() {
        return workspaceLocation;
    }

    public String getResultFileLocation() {
        return resultFileLocation;
    }

    public String getUnzippedSourceLocation() {
        return unzippedSourceLocation;
    }

    public String getZippedSourceLocation() {
        return zippedSourceLocation;
    }
}
