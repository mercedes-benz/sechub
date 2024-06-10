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

    @Autowired
    JGitAdapter jGitAdapter;

    @Autowired
    PDSProcessAdapterFactory processAdapterFactory;

    @Autowired
    DirectoryAndFileSupport directoryAndFileSupport;

    public void downloadRemoteData(GitContext gitContext) {
        jGitAdapter.clone(gitContext);
    }

    public void removeGitFiles(Path gitDownloadDirectory) throws IOException {
        if (gitDownloadDirectory == null) {
            return;
        }
        File parentDirectory = gitDownloadDirectory.toFile();

        directoryAndFileSupport.cleanDirectories(parentDirectory, AutoCleanupGitFilesFilter.INSTANCE);
    }

}
