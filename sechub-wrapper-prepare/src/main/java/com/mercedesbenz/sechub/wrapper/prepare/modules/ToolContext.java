// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SealedObject;

public abstract class ToolContext {

    private static final String UPLOAD_DIRECTORY_NAME = "upload";
    private String location;
    protected Path uploadDirectory;
    protected Path toolDownloadDirectory;

    private Map<String, SealedObject> credentialMap = new HashMap<>();

    public void setLocation(String location) {
        this.location = location;
    }

    public void setupRequiredToolDirectories(Path workingDirectory) {
        if (workingDirectory == null) {
            throw new IllegalArgumentException("Upload directory may not be null!");
        }
        this.uploadDirectory = workingDirectory.resolve(UPLOAD_DIRECTORY_NAME);
    }

    public void setCredentialMap(Map<String, SealedObject> credentialMap) {
        this.credentialMap = credentialMap;
    }

    public String getLocation() {
        return location;
    }

    public Path getUploadDirectory() {
        return uploadDirectory;
    }

    public Map<String, SealedObject> getCredentialMap() {
        return Collections.unmodifiableMap(credentialMap);
    }

    public Path getToolDownloadDirectory() {
        return toolDownloadDirectory;
    }

}
