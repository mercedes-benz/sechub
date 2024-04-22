package com.mercedesbenz.sechub.wrapper.prepare.moduls;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.pds.PDSProcessAdapterFactory;

@Component
public class PrepareWrapperGIT {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareWrapperGIT.class);

    @Value("${" + KEY_PDS_PREPARE_CREDENTIAL_USERNAME + "}")
    private String username;

    @Value("${" + KEY_PDS_PREPARE_CREDENTIAL_PASSWORD + "}")
    private String password;

    // TODO: 22.04.24 laura I dont like the idea of injecting the value here, but I
    // dont know how to do it better (what if I want to set a default?)
    @Value("${" + KEY_PDS_PREPARE_UPLOAD_FOLDER_DIRECTORY + "}")
    private String pdsPrepareUploadFolderDirectory;

    @Autowired
    PDSProcessAdapterFactory processAdapterFactory;

    public void cloneRepository(String location) throws IOException {
        List<String> commands = new ArrayList<>();
        String repositoryURL = getRepositoryURL(location);
        commands.add("git");
        commands.add("clone");
        commands.add("--depth 1");
        commands.add(repositoryURL);
        commands.add(pdsPrepareUploadFolderDirectory);

        ProcessBuilder builder = new ProcessBuilder(commands);
        try {
            processAdapterFactory.startProcess(builder);
        } catch (IOException e) {
            LOG.error("Error while cloning repository: {}", location, e);
            throw e;
        }
    }

    public void cleanGitDirectory(String targetFolder) throws IOException {
        List<String> commands = new ArrayList<>();
        String gitFiles = targetFolder + "/*.git*";
        commands.add("rm");
        commands.add("-rf");
        commands.add(gitFiles);

        ProcessBuilder builder = new ProcessBuilder(commands);
        try {
            processAdapterFactory.startProcess(builder);
        } catch (IOException e) {
            LOG.error("Error while cleaning git directory: {}", targetFolder, e);
            throw e;
        }
    }

    public void setEnvironmentVariables(String key, String value) {
        ProcessBuilder builder = new ProcessBuilder();
        Map<String, String> environment = builder.environment();
        environment.put(key, value);
    }

    private String getRepositoryURL(String location) {

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return "";
        }

        LOG.debug("Prepare location string for private repository");
        String preFix = "https://" + username + ":" + password + "@";

        String repositoryURL;
        if (location.contains("https://")) {
            repositoryURL = location.replace("https://", preFix);
        } else if (location.contains("git@")) {
            repositoryURL = location.replace("git@", preFix);
        } else if (location.contains("http://")) {
            repositoryURL = location.replace("http://", preFix);
        } else if (location.contains("git://")) {
            repositoryURL = location.replace("git://", preFix);
        } else {
            repositoryURL = preFix + location;
        }
        return repositoryURL;
    }
}
