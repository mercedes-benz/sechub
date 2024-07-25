// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import java.nio.file.Path;

import com.mercedesbenz.sechub.wrapper.prepare.modules.AbstractPrepareToolContext;

public class GitContext extends AbstractPrepareToolContext {

    static final String DOWNLOAD_DIRECTORY_NAME = "git-download";
    static final String DEFAULT_REPOSITORY_NAME = "git-repository";
    private boolean cloneWithoutHistory;
    private String repositoryName = DEFAULT_REPOSITORY_NAME;
    private Path toolDownloadDirectory;

    public void setCloneWithoutHistory(boolean cloneWithoutHistory) {
        this.cloneWithoutHistory = cloneWithoutHistory;
    }

    @Override
    public void init(Path workingDirectory) {
        super.init(workingDirectory);
        toolDownloadDirectory = workingDirectory.resolve(DOWNLOAD_DIRECTORY_NAME);
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        if (!(repositoryName == null || repositoryName.isBlank())) {
            this.repositoryName = repositoryName;
        }
    }

    public boolean isCloneWithoutHistory() {
        return cloneWithoutHistory;
    }

    @Override
    public Path getToolDownloadDirectory() {
        return toolDownloadDirectory;
    }

}
