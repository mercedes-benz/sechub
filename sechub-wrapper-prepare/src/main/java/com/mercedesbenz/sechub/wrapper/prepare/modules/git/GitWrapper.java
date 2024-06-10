// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.pds.PDSProcessAdapterFactory;
import com.mercedesbenz.sechub.commons.pds.ProcessAdapter;
import com.mercedesbenz.sechub.wrapper.prepare.modules.AbstractToolWrapper;

@Component
public class GitWrapper extends AbstractToolWrapper {

    @Autowired
    JGitAdapter jGitAdapter;

    @Autowired
    PDSProcessAdapterFactory processAdapterFactory;

    public void downloadRemoteData(GitContext gitContext) {
        jGitAdapter.clone(gitContext);
    }

    public void cleanUploadDirectory(Path gitDownloadDirectory) throws IOException {
        final ProcessBuilder builder = buildProcessClean(gitDownloadDirectory);
        ProcessAdapter process = null;

        try {
            process = processAdapterFactory.startProcess(builder);
        } catch (IOException e) {
            throw new IOException("Error while cleaning git directory: " + gitDownloadDirectory, e);
        }

        waitForProcessToFinish(process);
    }

    /*
     * FIXME Albert Tregnaghi, 2024-06-07: is this really necessary - workspace is
     * cleaned automatically, also this is process builder again?
     */
    private ProcessBuilder buildProcessClean(Path gitDownloadDirectory) {
        List<String> commands = new ArrayList<>();
        /*
         * FIXME Albert Tregnaghi, 2024-06-07: either with java api or let pds clean
         * workspace...
         */
        // removes recursively all git files from a directory
        // ( find . -type d -name ".git" && find . -name ".gitignore" && find . -name
        // ".gitmodules" ) | xargs rm -rf

        commands.add("/bin/bash");
        commands.add("-c");
        commands.add("( find . -type d -name .git && find . -name .gitignore && find . -name .gitattributes ) | xargs rm -rf");
        /* FIXME Albert Tregnaghi, 2024-06-07: java impl */
        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.directory(gitDownloadDirectory.toFile());
        builder.inheritIO();
        return builder;
    }
}
