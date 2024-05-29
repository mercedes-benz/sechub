// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SealedObject;

public abstract class ToolContext {
    private String location;
    private String uploadDirectory;
    private Map<String, SealedObject> credentialMap;

    public ToolContext(ToolContextBuilder builder) {
        this.location = builder.location;
        this.uploadDirectory = builder.uploadDirectory;
        this.credentialMap = builder.credentialMap;
    }

    public String getLocation() {
        return location;
    }

    public String getUploadDirectory() {
        return uploadDirectory;
    }

    public Map<String, SealedObject> getCredentialMap() {
        return Collections.unmodifiableMap(credentialMap);
    }

    public abstract static class ToolContextBuilder {
        private String location;
        private String uploadDirectory;
        private Map<String, SealedObject> credentialMap = new HashMap<>();

        public abstract ToolContext build();

        public ToolContextBuilder setLocation(String location) {
            if (location == null || location.isEmpty()) {
                throw new IllegalArgumentException("Defined Location must not be null or empty.");
            }
            this.location = location;
            return this;
        }

        public ToolContextBuilder setUploadDirectory(String uploadDirectory) {
            if (uploadDirectory == null || uploadDirectory.isEmpty()) {
                throw new IllegalArgumentException("Defined PDS Prepare Upload Directory must not be null or empty.");
            }
            this.uploadDirectory = uploadDirectory;
            return this;
        }

        public ToolContextBuilder setCredentialMap(Map<String, SealedObject> credentialMap) {
            if (!(credentialMap == null)) {
                this.credentialMap = credentialMap;
            }
            return this;
        }
    }
}
