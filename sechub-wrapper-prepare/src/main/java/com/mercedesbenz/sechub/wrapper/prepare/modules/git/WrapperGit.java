// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.pds.PDSProcessAdapterFactory;
import com.mercedesbenz.sechub.commons.pds.ProcessAdapter;
import com.mercedesbenz.sechub.wrapper.prepare.modules.WrapperTool;

@Component
public class WrapperGit extends WrapperTool {

    private static final Logger LOG = LoggerFactory.getLogger(WrapperGit.class);

    @Autowired
    JGitAdapter jGitAdapter;

    @Autowired
    PDSProcessAdapterFactory processAdapterFactory;

    public void downloadRemoteData(GitContext gitContext) {
        LOG.debug("Start cloning with JGit.");
        jGitAdapter.clone(gitContext);
    }

    public void cleanUploadDirectory(String uploadDirectory) throws IOException {
        final ProcessBuilder builder = buildProcessClean(uploadDirectory);
        ProcessAdapter process = null;

        try {
            process = processAdapterFactory.startProcess(builder);
        } catch (IOException e) {
            throw new IOException("Error while cleaning git directory: " + uploadDirectory, e);
        }

        waitForProcessToFinish(process);
    }

    private ProcessBuilder buildProcessClean(String pdsPrepareUploadFolderDirectory) {
        List<String> commands = new ArrayList<>();
        // removes recursively all git files from a directory
        // ( find . -type d -name ".git" && find . -name ".gitignore" && find . -name
        // ".gitmodules" ) | xargs rm -rf

        File uploadDir = Paths.get(pdsPrepareUploadFolderDirectory).toAbsolutePath().toFile();

        commands.add("/bin/bash");
        commands.add("-c");
        commands.add("( find . -type d -name .git && find . -name .gitignore && find . -name .gitattributes ) | xargs rm -rf");

        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.directory(uploadDir);
        builder.inheritIO();
        return builder;
    }
}
