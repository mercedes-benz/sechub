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

@Component
public class WrapperGit extends WrapperTool {

    private static final Logger LOG = LoggerFactory.getLogger(WrapperGit.class);

    @Autowired
    JGitAdapter JGitAdapter;

    public void downloadRemoteData(GitContext gitContext) {
        String location = gitContext.getLocation();
        String uploadDirectory = gitContext.getUploadDirectory();
        Map<String, SealedObject> credentialMap = gitContext.getCredentialMap();

        escapeRepositoryURL(location, null);

        if (credentialMap != null && !credentialMap.isEmpty()) {
            String username = "";
            String password = "";

            for (Map.Entry<String, SealedObject> entry : credentialMap.entrySet()) {
                if (entry.getKey().equals(PDS_PREPARE_CREDENTIAL_USERNAME)) {
                    username = CryptoAccess.CRYPTO_STRING.unseal(entry.getValue());
                } else if (entry.getKey().equals(PDS_PREPARE_CREDENTIAL_PASSWORD)) {
                    password = CryptoAccess.CRYPTO_STRING.unseal(entry.getValue());
                }
            }
            LOG.debug("Cloning private repository: " + location + " to " + uploadDirectory);
            JGitAdapter.clonePrivate(gitContext, username, password);
        } else {
            LOG.debug("Cloning public repository: " + location + " to " + uploadDirectory);
            JGitAdapter.clonePublic(gitContext);
        }
    }

    public void cleanUploadDirectory(String uploadDirectory) throws IOException {

        final ProcessBuilder builder = buildProcessClean(uploadDirectory);

        try {
            process = processAdapterFactory.startProcess(builder);
        } catch (IOException e) {
            throw new IOException("Error while cleaning git directory: " + uploadDirectory, e);
        }

        waitForProcessToFinish();
    }

    private ProcessBuilder buildProcessClean(String pdsPrepareUploadFolderDirectory) {
        List<String> commands = new ArrayList<>();
        // removes recursively all git files from a directory
        // ( find . -type d -name ".git" && find . -name ".gitignore" && find . -name
        // ".gitmodules" ) | xargs rm -rf

        File uploadDir = Paths.get(pdsPrepareUploadFolderDirectory).toAbsolutePath().toFile();

        commands.add("bin/bash");
        commands.add("-c");
        commands.add("( find . -type d -name .git && find . -name .gitignore && find . -name .gitattributes ) | xargs rm -rf");

        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.directory(uploadDir);
        builder.inheritIO();
        return builder;
    }
}
