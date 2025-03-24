// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.archive.DirectoryAndFileSupport;
import com.mercedesbenz.sechub.commons.pds.PDSProcessAdapterFactory;
import com.mercedesbenz.sechub.wrapper.prepare.modules.AbstractToolWrapper;

@Component
public class GitWrapper extends AbstractToolWrapper {

    private final JGitAdapter jGitAdapter;

    private final PDSProcessAdapterFactory processAdapterFactory;

    private final DirectoryAndFileSupport directoryAndFileSupport;

    public GitWrapper(JGitAdapter jGitAdapter, PDSProcessAdapterFactory processAdapterFactory, DirectoryAndFileSupport directoryAndFileSupport) {
        this.jGitAdapter = jGitAdapter;
        this.processAdapterFactory = processAdapterFactory;
        this.directoryAndFileSupport = directoryAndFileSupport;
    }

    public void downloadRemoteData(GitContext gitContext) {
        jGitAdapter.clone(gitContext);
    }

    public void removeAdditionalGitFiles(Path gitDownloadDirectory) throws IOException {
        if (gitDownloadDirectory == null) {
            return;
        }
        File parentDirectory = gitDownloadDirectory.toFile();

        directoryAndFileSupport.cleanDirectories(parentDirectory, AutoCleanupAdditionalGitFilesFilter.INSTANCE);
    }

    public void removeGitFolders(Path gitDownloadDirectory) throws IOException {
        if (gitDownloadDirectory == null) {
            return;
        }
        File parentDirectory = gitDownloadDirectory.toFile();

        directoryAndFileSupport.cleanDirectories(parentDirectory, AutoCleanupGitFoldersFilter.INSTANCE);
    }

}
