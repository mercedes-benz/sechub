// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractSecHubFileSystemContainer implements SecHubFileSystemContainer {

    public static final String PROPERTY_FILESYSTEM = "fileSystem";
    public static final String PROPERTY_INCLUDES = "includes";
    public static final String PROPERTY_EXCLUDES = "excludes";

    private Optional<SecHubFileSystemConfiguration> fileSystem = Optional.empty();

    @JsonInclude(Include.NON_EMPTY) // when no excludes defined then they should not appear in payload
    private List<String> excludes = new ArrayList<>();

    @JsonInclude(Include.NON_EMPTY) // when no includes defined then they should not appear in payload
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
