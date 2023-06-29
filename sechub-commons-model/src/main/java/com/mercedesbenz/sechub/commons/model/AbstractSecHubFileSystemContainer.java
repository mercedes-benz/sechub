// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractSecHubFileSystemContainer implements SecHubFileSystemContainer {

    public static final String PROPERTY_FILESYSTEM = "fileSystem";

    private Optional<SecHubFileSystemConfiguration> fileSystem = Optional.empty();
    
    private List<String> excludes = new ArrayList<>();
    private List<String> includes = new ArrayList<>();

    public void setFileSystem(SecHubFileSystemConfiguration fileSystem) {
        this.fileSystem = Optional.ofNullable(fileSystem);
    }

    @Override
    public Optional<SecHubFileSystemConfiguration> getFileSystem() {
        return fileSystem;
    }
    
    public List<String> getIncludes() {
        return includes;
    }
    
    public List<String> getExcludes() {
        return excludes;
    }
}
