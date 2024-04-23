package com.mercedesbenz.sechub.wrapper.prepare.moduls;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.pds.PDSProcessAdapterFactory;
import com.mercedesbenz.sechub.commons.pds.ProcessAdapter;

@Component
public class PrepareWrapperGIT {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareWrapperGIT.class);

    private ProcessAdapter process;

    private int minutesToWaitForResult = 30;

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

    public ProcessAdapter getProcessAdapter() {
        return process;
    }

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
            process = processAdapterFactory.startProcess(builder);
        } catch (IOException e) {
            LOG.error("Error while cloning repository: " + location, e);
            throw new IOException("Error while cloning repository: " + location, e);
        }
        waitForProcessToFinish();
    }

    public void cleanGitDirectory() throws IOException {
        List<String> commands = new ArrayList<>();
        // removes recursively all git files from a directory (rm -rf *.git* does not
        // work recursively)
        // ( find . -type d -name ".git" && find . -name ".gitignore" && find . -name
        // ".gitmodules" ) | xargs rm -rf

        String[] gitFiles = { ".git", ".gitignore", ".gitattributes" };

        commands.add("(");
        for (int i = 0; i < gitFiles.length; i++) {
            commands.add("find");
            commands.add(pdsPrepareUploadFolderDirectory);
            commands.add("-type d -name");
            commands.add(gitFiles[i]);
            if (i == gitFiles.length - 1) {
                continue;
            }
            commands.add("&&");
        }
        commands.add(") |");
        commands.add("xargs rm -rf");

        ProcessBuilder builder = new ProcessBuilder(commands);
        try {
            process = processAdapterFactory.startProcess(builder);
        } catch (IOException e) {
            LOG.error("Error while cleaning git directory: " + pdsPrepareUploadFolderDirectory, e);
            throw new IOException("Error while cleaning git directory: " + pdsPrepareUploadFolderDirectory, e);
        }
        waitForProcessToFinish();
    }

    public void setEnvironmentVariables(String key, String value) throws IOException {
        ProcessBuilder builder = new ProcessBuilder();
        Map<String, String> environment = builder.environment();
        environment.put(key, value);
        try {
            process = processAdapterFactory.startProcess(builder);
        } catch (IOException e) {
            LOG.error("Error while exporting environment variable: " + key + ":" + value, e);
            throw new IOException("Error while exporting environment variable: " + key + ":" + value, e);
        }
        waitForProcessToFinish();
    }

    String getRepositoryURL(String location) {

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            LOG.debug("Username and Password not set. Repository URL does not need to be prepared.");
            return location;
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

    private void waitForProcessToFinish() {

        /* watching */
        String watcherThreadName = "PDSPrepareWrapper-git-watcher";

        LOG.debug("Start watcher thread: {}", watcherThreadName);

        StreamDataRefreshRequestWatcherRunnable streamDatawatcherRunnable = new StreamDataRefreshRequestWatcherRunnable();
        Thread streamDataWatcherThread = new Thread(streamDatawatcherRunnable);
        streamDataWatcherThread.setName(watcherThreadName);
        streamDataWatcherThread.start();

        /* waiting for process */
        LOG.debug("Wait for GIT wrapper to finish process.");

        boolean exitDoneInTime = false;
        try {
            exitDoneInTime = process.waitFor(minutesToWaitForResult, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException("GIT wrapper could not finish process.", e);
        }

        if (!exitDoneInTime) {
            LOG.error("GIT wrapper could not finish process. Waited {} minutes.", minutesToWaitForResult);
            throw new RuntimeException("GIT wrapper could not finish process. Waited " + minutesToWaitForResult + " minutes.");
        }
        streamDatawatcherRunnable.stop();
    }

    private static class StreamDataRefreshRequestWatcherRunnable implements Runnable {

        private boolean stopped;

        @Override
        public void run() {
            while (!stopped && !Thread.currentThread().isInterrupted()) {
                LOG.trace("start checking stream data refresh requests");
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        public void stop() {
            stopped = true;
        }

    }
}
