// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import java.nio.file.Path;

import com.mercedesbenz.sechub.wrapper.prepare.modules.ToolContext;

public class GitContext extends ToolContext {

    static final String DOWNLOAD_DIRECTORY_NAME = "git-download";
    private boolean cloneWithoutHistory;
    private String repositoryName = "git-repository";

    public void setCloneWithoutHistory(boolean cloneWithoutHistory) {
        this.cloneWithoutHistory = cloneWithoutHistory;
    }

    @Override
    public void setupRequiredToolDirectories(Path workingDirectory) {
        super.setupRequiredToolDirectories(workingDirectory);
        toolDownloadDirectory = workingDirectory.resolve(DOWNLOAD_DIRECTORY_NAME);
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public boolean isCloneWithoutHistory() {
        return cloneWithoutHistory;
    }

}
