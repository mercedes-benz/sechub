// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubFileSystemConfiguration {

    public static final String PROPERTY_FOLDERS = "folders";
    public static final String PROPERTY_FILES = "files";

    private List<String> files = new ArrayList<>();
    private List<String> folders = new ArrayList<>();

    public List<String> getFolders() {
        return folders;
    }

    public List<String> getFiles() {
        return files;
    }

}
