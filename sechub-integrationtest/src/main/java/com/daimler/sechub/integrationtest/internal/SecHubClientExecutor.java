// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestUser;
import com.daimler.sechub.sharedkernel.type.TrafficLight;
import com.daimler.sechub.test.TestUtil;

import wiremock.com.google.common.io.Files;

public class SecHubClientExecutor {

    public enum ClientAction {

        START_ASYNC("scanAsync"),

        START_SYNC("scan"),

        GET_REPORT("getReport"),

        GET_STATUS("getStatus"),

        ;

        private String command;

        private ClientAction(String command) {
            this.command = command;
        }

        public String getCommand() {
            return command;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(SecHubClientExecutor.class);
    private Path outputFolder;

    public class ExecutionResult {
        private int exitCode;
        private String lastOutputLine;
        private File outputFolder;
        public UUID sechubJobUUD;
        public String[] lines;

        public int getExitCode() {
            return exitCode;
        }

        public File getOutputFolder() {
            return outputFolder;
        }

        public String getLastOutputLine() {
            if (lastOutputLine == null) {
                lastOutputLine = lines[lines.length - 1];
            }
            return lastOutputLine;
        }

        public UUID getSechubJobUUD() {
            if (sechubJobUUD == null) {
                for (String line : lines) {
                    if (sechubJobUUD != null) {
                        break;
                    }
                    int index = line.indexOf("job ");
                    if (index != -1) {
                        try {
                            String remaining = line.substring(index + 4).split(" ")[0];
                            sechubJobUUD = UUID.fromString(remaining);
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return sechubJobUUD;
        }

        public File getJSONReportFile() {
            UUID jobUUID = getSechubJobUUD();
            if (jobUUID == null) {
                fail("No job uuid found - last output line was:" + lastOutputLine);
            }
            String jobUUIDString = jobUUID.toString();
            File outputFile = new File(getOutputFolder(), "sechub_report_" + jobUUIDString + ".json");
            return outputFile;
        }

        public TrafficLight getTrafficLight() {

            String last = getLastOutputLine().trim().toUpperCase();
            for (TrafficLight light : TrafficLight.values()) {
                if (last.startsWith(light.name())) {
                    return light;
                }
            }
            return null;
        }
    }

    public ExecutionResult execute(File file, TestUser user, ClientAction action, Map<String, String> environmentVariables, String... options) {
        if (user.getApiToken() == null) {
            throw new IllegalStateException("Test user:" + user.getUserId()
                    + " has no apiToken. This can happen if you are using users from another scenario... Please check your test!");
        }

        File exampleScanRootFolder = ensureExampleContentFoldersExist();

        /* other folders are "synthetic" and created simply on demand: */

        // origin path of sechub client path:
        String path = "sechub-cli/build/go/platform/";
        List<String> commandsAsList = new ArrayList<>();
        String sechubExeName = null;
        if (TestUtil.isWindows()) {
            sechubExeName = "sechub.exe";
            path += "windows-386";
            commandsAsList.add("cmd.exe");
            commandsAsList.add("/C");
        } else {
            sechubExeName = "sechub";
            path += "linux-386";
        }
        File executableParentFolder = new File(IntegrationTestFileSupport.getTestfileSupport().getRootFolder(), path);
        File executableFile = new File(executableParentFolder, sechubExeName);
        if (!executableFile.exists()) {
            throw new SecHubClientNotFoundException(executableFile);
        }
        commandsAsList.add(0, executableFile.getAbsolutePath());

        if (file != null) {
            commandsAsList.add("-configfile");
            if (TestUtil.isWindows()) {
                commandsAsList.add("\"" + file.getAbsolutePath() + "\"");
            } else {
                commandsAsList.add(file.getAbsolutePath());
            }
        }
        if (user != null) {
            commandsAsList.add("-user");
            commandsAsList.add(user.getUserId());

            commandsAsList.add("-apitoken");
            commandsAsList.add(user.getApiToken());
        }

        commandsAsList.addAll(Arrays.asList(options));

        if (action != null) {
            commandsAsList.add(action.getCommand());
        }

        try {
            /* create temp file for output */
            File tmpGoOutputFile = File.createTempFile("sechub-client-test-", ".txt");
            tmpGoOutputFile.deleteOnExit();
            LOG.info("Temporary go output at:{}", tmpGoOutputFile);

            /* setup process */
            ProcessBuilder pb = new ProcessBuilder(commandsAsList);
            pb.redirectErrorStream(true);
            pb.redirectOutput(tmpGoOutputFile);
            Map<String, String> environment = pb.environment();
            environment.put("SECHUB_TRUSTALL", "true");
            if (IntegrationTestSetup.SECHUB_CLIENT_DEBUGGING_ENABLED) {
                // we enable only when explicit wanted - so logs are smaller and easier to read
                environment.put("SECHUB_DEBUG", "true");
            }
            if (TestUtil.isKeepingTempfiles()) {
                environment.put("SECHUB_KEEP_TEMPFILES", "true");
            }
            if (environmentVariables != null) {
                environment.putAll(environmentVariables);
            }
            pb.directory(exampleScanRootFolder);

            StringBuilder sb = new StringBuilder();
            Iterator<String> it = commandsAsList.iterator();
            while (it.hasNext()) {
                sb.append(it.next());
                if (it.hasNext()) {
                    sb.append(" ");
                }
            }
            LOG.info("Execute command '{}'", sb.toString());

            /* start process and wait for execution done */
            Process process = pb.start();
            int exitCode = process.waitFor();

            /* show go output */
            String output = IntegrationTestFileSupport.getTestfileSupport().loadTextFile(tmpGoOutputFile, "\n");
            LOG.info("Command output:\n{}", output);

            /* prepare and return result */
            ExecutionResult result = new ExecutionResult();
            result.lines = output.trim().split("\\n");

            result.exitCode = exitCode;
            result.outputFolder = outputFolder.toFile();

            return result;
        } catch (IOException e) {
            LOG.error("io failure on command execution", e);
            throw new IllegalStateException("Execution failed", e);
        } catch (InterruptedException e) {
            LOG.error("interrupted command execution", e);
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Execution failed", e);
        }
    }

    private File ensureExampleContentFoldersExist() {
        /*
         * because SecHub client checks for existing folders we must ensure integration
         * test do scan existing folders so we ensure example content/folders exists
         */
        String pathToExamples = "sechub-integrationtest/build/sechub/example/content/"; // same deepness as "sechub-cli/build/go/platform/linux-386"
        File exampleScanRootFolder = new File(IntegrationTestFileSupport.getTestfileSupport().getRootFolder(), pathToExamples);
        exampleScanRootFolder.mkdirs();

        for (IntegrationTestExampleFolders folder : IntegrationTestExampleFolders.values()) {
            File projectResourceFoldder = new File(exampleScanRootFolder, folder.getPath());
            if (folder.isExistingContent()) {
                assertTrue("This projectResourceFolder must already exist but doesnt:" + projectResourceFoldder.getAbsolutePath(),
                        projectResourceFoldder.isDirectory() && projectResourceFoldder.exists());
            } else {
                projectResourceFoldder.mkdirs();// we generate this one
                File testFile1 = new File(projectResourceFoldder, "TestMeIfYouCan.java");
                if (!testFile1.exists()) {
                    try {
                        Files.write("class TestMeifYouCan {}", testFile1, Charset.forName("UTF-8"));
                    } catch (IOException e) {
                        throw new IllegalStateException("Cannot create test output!", e);
                    }

                }
            }
        }
        return exampleScanRootFolder;
    }

    public void setOutputFolder(Path outputFolder) {
        this.outputFolder = outputFolder;
    }

    public static void main(String[] args) {
        new SecHubClientExecutor().execute(null, null, null, null, "-help");
    }

    public class SecHubClientNotFoundException extends IllegalStateException {
        private static final long serialVersionUID = 1L;

        public SecHubClientNotFoundException(File executable) {
            super("SecHub client not available, did you forget to build the client with `gradlew buildGo` ?\nExpected:" + executable);
        }
    }

}
