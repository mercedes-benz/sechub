package com.mercedesbenz.sechub.commons.model;

import java.util.Optional;

public abstract class AbstractSecHubFileSystemContainer {

    public static final String PROPERTY_FILESYSTEM = "fileSystem";
    
    private Optional<SecHubFileSystemConfiguration> fileSystem = Optional.empty();

    public void setFileSystem(SecHubFileSystemConfiguration fileSystem) {
        this.fileSystem = Optional.ofNullable(fileSystem);
    }

    public Optional<SecHubFileSystemConfiguration> getFileSystem() {
        return fileSystem;
    }
}
