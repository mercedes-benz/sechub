package com.mercedesbenz.sechub.wrapper.checkmarx.scan;

import java.io.InputStream;
import java.util.Set;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.wrapper.checkmarx.CheckmarxWrapperEnvironment;

public class CheckmarxWrapperContext {

    private SecHubConfigurationModel configuration;
    private CheckmarxWrapperEnvironment environment;

    CheckmarxWrapperContext(SecHubConfigurationModel configuration, CheckmarxWrapperEnvironment environment) {
        this.configuration = configuration;
        this.environment = environment;
    }

    public Set<String> createCodeUploadFileSystemFolders() {
        return null;
    }

    public InputStream createSourceCodeZipFileInputStream() {
        // TODO Auto-generated method stub
        return null;
    }

    public CheckmarxWrapperEnvironment getEnvironment() {
        return environment;
    }

    public SecHubConfigurationModel getConfiguration() {
        return configuration;
    }
}
