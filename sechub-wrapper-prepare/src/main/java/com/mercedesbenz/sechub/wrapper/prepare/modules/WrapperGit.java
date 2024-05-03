package com.mercedesbenz.sechub.wrapper.prepare.modules;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironmentVariables.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.crypto.SealedObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.pds.ProcessAdapter;

@Component
public class WrapperGit extends WrapperTool {

    private static final Logger LOG = LoggerFactory.getLogger(WrapperGit.class);

    @Autowired
    JGitAdapter JGitAdapter;

    public void downloadRemoteData(GitContext gitContext) {
        String location = gitContext.getLocation();
        String uploadDirectory = gitContext.getUploadDirectory();
        Map<String, SealedObject> credentialMap = gitContext.getCredentialMap();

        if (credentialMap == null | credentialMap.isEmpty()) {
            LOG.debug("Cloning public repository: " + location + " to " + uploadDirectory);
            JGitAdapter.clonePublicRepository(gitContext);

        } else {

            String username = CryptoAccess.CRYPTO_STRING.unseal(credentialMap.get(PDS_PREPARE_CREDENTIAL_USERNAME));
            String password = CryptoAccess.CRYPTO_STRING.unseal(credentialMap.get(PDS_PREPARE_CREDENTIAL_PASSWORD));
            if (username == null || password == null) {
                throw new IllegalArgumentException("Username and password must be provided for private repository.");
            }

            LOG.debug("Cloning private repository: " + location + " to " + uploadDirectory);
            JGitAdapter.clonePrivateRepository(gitContext, username, password);
        }
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
