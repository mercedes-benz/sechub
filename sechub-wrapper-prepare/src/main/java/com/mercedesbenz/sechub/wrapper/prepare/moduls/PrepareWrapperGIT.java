package com.mercedesbenz.sechub.wrapper.prepare.moduls;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironmentVariables.*;
import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.SealedObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.pds.PDSProcessAdapterFactory;
import com.mercedesbenz.sechub.commons.pds.ProcessAdapter;

@Component
public class PrepareWrapperGIT {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareWrapperGIT.class);

    private ProcessAdapter process;

    @Value("${" + KEY_PDS_PREPARE_MINUTES_TO_WAIT_PREPARE_PROCESSES + ":30}")
    private int minutesToWaitForResult;

    @Autowired
    PDSProcessAdapterFactory processAdapterFactory;

    public void cloneRepository(GitContext gitContext) throws IOException {
        String repositoryURL = gitContext.location;
        List<String> commands = new ArrayList<>();
        if (gitContext.credentialMap != null && !gitContext.credentialMap.isEmpty()) {
            repositoryURL = getRepositoryURL(gitContext.location);
        }

        commands.add("bash");
        commands.add("-c");
        if (gitContext.cloneWithoutHistory) {
            commands.add("git clone --depth 1 " + repositoryURL + " " + gitContext.pdsPrepareUploadFolderDirectory);
        } else {
            commands.add("git clone " + repositoryURL + " " + gitContext.pdsPrepareUploadFolderDirectory);
        }

        ProcessBuilder builder = new ProcessBuilder(commands);

        Map<String, String> environment = builder.environment();
        if (gitContext.credentialMap != null && !gitContext.credentialMap.isEmpty()) {
            for (Map.Entry<String, SealedObject> entry : gitContext.credentialMap.entrySet()) {
                try {
                    environment.put(entry.getKey(), CryptoAccess.CRYPTO_STRING.unseal(entry.getValue()));
                } catch (Exception e) {
                    LOG.error("Error while unsealing credential: " + entry.getKey(), e);
                    throw new IOException("Error while unsealing credential: " + entry.getKey(), e);
                }
            }
        }

        try {
            process = processAdapterFactory.startProcess(builder);
        } catch (IOException e) {
            LOG.error("Error while cloning repository: " + gitContext.location, e);
            throw new IOException("Error while cloning repository: " + gitContext.location + "GIT Error: " + e.getMessage(), e);
        }
        waitForProcessToFinish();
    }

    public void cleanGitDirectory(String pdsPrepareUploadFolderDirectory) throws IOException {
        List<String> commands = new ArrayList<>();
        // removes recursively all git files from a directory
        // ( find . -type d -name ".git" && find . -name ".gitignore" && find . -name
        // ".gitmodules" ) | xargs rm -rf

        File uploadDir;
        uploadDir = Paths.get(pdsPrepareUploadFolderDirectory).toAbsolutePath().toFile();

        commands.add("bash");
        commands.add("-c");
        commands.add("( find . -type d -name .git && find . -name .gitignore && find . -name .gitattributes ) | xargs rm -rf");

        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.directory(uploadDir);
        try {
            process = processAdapterFactory.startProcess(builder);
        } catch (Exception e) {
            LOG.error("Error while cleaning git directory: " + pdsPrepareUploadFolderDirectory, e);
            throw new IOException("Error while cleaning git directory: " + pdsPrepareUploadFolderDirectory, e);
        }
        waitForProcessToFinish();
    }

    public GitContext createGitContext(String location, boolean cloneWithoutHistory, Map<String, SealedObject> credentialMap,
            String pdsPrepareUploadFolderDirectory) {
        return new GitContext(location, cloneWithoutHistory, credentialMap, pdsPrepareUploadFolderDirectory);
    }

    String getRepositoryURL(String location) {
        LOG.debug("Prepare location string for private repository");
        String preFix = "https://" + "$" + PDS_PREPARE_CREDENTIAL_USERNAME + ":" + "$" + PDS_PREPARE_CREDENTIAL_PASSWORD + "@";

        String repositoryURL;
        if (location.contains("https://")) {
            repositoryURL = location.replace("https://", preFix);
        } else if (location.contains("git@")) {
            location = location.replace(":", "/");
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
        checkProcessExitValue(process.exitValue());
    }

    private void checkProcessExitValue(int exitValue) {
        String result = "";
        if (exitValue == 0) {
            result = "GIT wrapper finished process successfully.";
        } else {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getProcess().getErrorStream()))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line = null;
                while (true) {
                    try {
                        if ((line = reader.readLine()) == null)
                            break;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    stringBuilder.append(line);
                    stringBuilder.append(System.getProperty("line.separator"));
                }
                result = stringBuilder.toString();
            } catch (IOException e) {
                throw new RuntimeException(result, e);
            }
            throw new RuntimeException(result);
        }
        LOG.debug(result);
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
