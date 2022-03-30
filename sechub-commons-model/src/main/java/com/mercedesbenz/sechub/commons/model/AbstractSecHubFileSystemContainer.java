package com.mercedesbenz.sechub.commons.model;

import java.util.Optional;

public abstract class AbstractSecHubFileSystemContainer implements SecHubFileSystemContainer {

    public static final String PROPERTY_FILESYSTEM = "fileSystem";
    
    private Optional<SecHubFileSystemConfiguration> fileSystem = Optional.empty();

    public void setFileSystem(SecHubFileSystemConfiguration fileSystem) {
        this.fileSystem = Optional.ofNullable(fileSystem);
    }

    @Override
    public Optional<SecHubFileSystemConfiguration> getFileSystem() {
        return fileSystem;
    }
}
