package com.mercedesbenz.sechub.wrapper.prepare.modules.test;

import java.nio.file.Path;

import com.mercedesbenz.sechub.wrapper.prepare.modules.ToolContext;

public class IntegrationTestContext extends ToolContext {
    static final String DOWNLOAD_DIRECTORY_NAME = "integration-test-download";
    private String repositoryName = "integration-test-repository";

    @Override
    public void setupRequiredToolDirectories(Path workingDirectory) {
        super.setupRequiredToolDirectories(workingDirectory);
        toolDownloadDirectory = workingDirectory.resolve(DOWNLOAD_DIRECTORY_NAME);
    }

    public String getRepositoryName() {
        return repositoryName;
    }

}
