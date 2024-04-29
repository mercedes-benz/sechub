package com.mercedesbenz.sechub.wrapper.prepare.moduls;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironmentVariables.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.crypto.SealedObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WrapperGit extends WrapperTool {

    private static final Logger LOG = LoggerFactory.getLogger(WrapperGit.class);
    private final List<String> forbiddenCharacters = Arrays.asList(">", "<", "!", "?", "*", "'", "\"", ";", "&", "|");

    public void downloadRemoteData(ContextGit contextGit) throws IOException {

        String location = contextGit.getLocation();
        Map<String, SealedObject> credentialMap = contextGit.getCredentialMap();
        String repositoryURL = location;

        if (credentialMap != null && !credentialMap.isEmpty()) {
            repositoryURL = transformGitRepositoryURL(location);
        }

        escapeRepositoryURL(repositoryURL, forbiddenCharacters);

        final ProcessBuilder builder = buildProcessClone(repositoryURL, contextGit);
        exportEnvironmentVariables(builder, credentialMap);

        try {
            process = processAdapterFactory.startProcess(builder);
        } catch (IOException e) {
            throw new IOException("Error while cloning repository: " + location + "\nGIT Error: " + e.getMessage(), e);
        }

        waitForProcessToFinish();
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

    String transformGitRepositoryURL(String location) {
        LOG.debug("Prepare location string for private repository");
        String preFix = "https://" + "$" + PDS_PREPARE_CREDENTIAL_USERNAME + ":" + "$" + PDS_PREPARE_CREDENTIAL_PASSWORD + "@";

        String https = "https://";
        String git_ssh = "git@";
        String http = "http://";
        String git = "git://";

        String repositoryURL;
        if (location.contains(https)) {
            repositoryURL = location.replace(https, preFix);
        } else if (location.contains(git_ssh)) {
            location = location.replace(":", "/");
            repositoryURL = location.replace(git_ssh, preFix);
        } else if (location.contains(http)) {
            repositoryURL = location.replace(http, preFix);
        } else if (location.contains(git)) {
            repositoryURL = location.replace(git, preFix);
        } else {
            repositoryURL = preFix + location;
        }
        return repositoryURL;
    }

    private ProcessBuilder buildProcessClean(String pdsPrepareUploadFolderDirectory) {
        List<String> commands = new ArrayList<>();
        // removes recursively all git files from a directory
        // ( find . -type d -name ".git" && find . -name ".gitignore" && find . -name
        // ".gitmodules" ) | xargs rm -rf

        File uploadDir = Paths.get(pdsPrepareUploadFolderDirectory).toAbsolutePath().toFile();

        commands.add("bash");
        commands.add("-c");
        commands.add("( find . -type d -name .git && find . -name .gitignore && find . -name .gitattributes ) | xargs rm -rf");

        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.directory(uploadDir);
        builder.inheritIO();
        return builder;
    }

    private ProcessBuilder buildProcessClone(String repositoryURL, ContextGit contextGit) {
        List<String> commands = new ArrayList<>();
        String uploadDirectory = contextGit.getUploadDirectory();

        commands.add("bash");
        commands.add("-c");
        if (contextGit.isCloneWithoutHistory()) {
            commands.add("git clone --depth 1 " + repositoryURL + " " + uploadDirectory);
        } else {
            commands.add("git clone " + repositoryURL + " " + uploadDirectory);
        }

        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.inheritIO();

        return builder;
    }
}
