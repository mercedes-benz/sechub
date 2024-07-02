// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.test;

import java.nio.file.Path;

import com.mercedesbenz.sechub.wrapper.prepare.modules.AbstractPrepareToolContext;

public class IntegrationTestContext extends AbstractPrepareToolContext {
    static final String DOWNLOAD_DIRECTORY_NAME = "integration-test-download";
    private String repositoryName = "integration-test-repository";
    private Path toolDownloadDirectory;

    @Override
    public void init(Path workingDirectory) {
        super.init(workingDirectory);
        toolDownloadDirectory = workingDirectory.resolve(DOWNLOAD_DIRECTORY_NAME);
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    @Override
    public Path getToolDownloadDirectory() {
        return toolDownloadDirectory;
    }

}
