// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSupport;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.api.WithSecHubClient.ApiTokenStrategy;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestExampleConstants.IntegrationTestExampleFolder;
import com.mercedesbenz.sechub.test.TestFileSupport;
import com.mercedesbenz.sechub.test.TestFileWriter;
import com.mercedesbenz.sechub.test.TestUtil;

public class SecHubClientExecutor {

    public enum ClientAction {

        START_ASYNC("scanAsync"),

        START_SYNC("scan"),

        GET_REPORT("getReport"),

        GET_STATUS("getStatus"),

        MARK_FALSE_POSITIVES("markFalsePositives"),

        UNMARK_FALSE_POSITIVES("unmarkFalsePositives"),

        GET_FALSE_POSITIVES("getFalsePositives"),

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
    private String sechubClientBinaryPath;
    private boolean trustAll;
    private File cachedExecutableFile;
    private static File cachedExampleContentFolder;

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

        public UUID getSechubJobUUID() {
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

        public File getJSONFalsePositiveFile() {
            // Output is like: false-positives list written to file
            // /tmp/with-sechub-client-755472131402648511/sechub-false-positives-scenario3_project1.json
            String lastoutputLine = getLastOutputLine();
            if (lastoutputLine == null) {
                fail("no last output line available!");
            }
            String marker = "written to file";
            int index = lastoutputLine.indexOf(marker);
            if (index == -1) {
                fail("unexpected last line, did not contain:" + marker + " but was:" + lastoutputLine);
            }
            String path = lastoutputLine.substring(index + marker.length());
            return new File(path.trim());
        }

        public File getJSONReportFile() {
            UUID jobUUID = getSechubJobUUID();
            if (jobUUID == null) {
                fail("No job uuid found - last output line was:" + lastOutputLine);
            }
            String jobUUIDString = jobUUID.toString();
            /*
             * at this point, we have no information about the project - but job uuid is
             * unique. so we just check the output directory for sechub report files with
             * this job uuid.
             */
            FilenameFilter filter = new SecHubReportFileNameFilter(jobUUID);
            File[] files = getOutputFolder().listFiles(filter);
            if (files.length > 1) {
                throw new IllegalStateException("There exist multiple report files with same job uuid?!?! This should never happen!");
            }
            if (files.length == 1) {
                return files[0]; // found it
            }
            /* we return this file when not found at all */
            return new File(getOutputFolder(), "fallback_not_existing__sechub_report_ANY_PROJECTID_" + jobUUIDString + ".json");
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

    public ExecutionResult execute(File file, ApiTokenStrategy apiTokenStrategy, TestUser user, ClientAction action, Map<String, String> environmentVariables,
            String... options) {
        assertAPITokenSet(user);

        File targetFolder = resolveScanTargetFolder(file);
        List<String> commandsAsList = new ArrayList<>();

        handleExecutable(commandsAsList);
        handleConfigFileCommand(file, commandsAsList);
        handleUserCredentials(apiTokenStrategy, user, environmentVariables, commandsAsList);
        handleOptions(commandsAsList, options);
        handleAction(action, commandsAsList);

        return execute(environmentVariables, targetFolder, commandsAsList);
    }

    private ExecutionResult execute(Map<String, String> environmentVariables, File targetFolder, List<String> commandsAsList) {
        try {
            File tmpGoOutputFile = createTempFileForOutput();

            ProcessBuilder pb = createProcessBuilder(environmentVariables, targetFolder, commandsAsList, tmpGoOutputFile);

            logCommand(commandsAsList);

            /* start process and wait for execution done */
            Process process = pb.start();
            long started = System.currentTimeMillis();
            int exitCode = process.waitFor();
            long elapsed = System.currentTimeMillis() - started;
            LOG.debug("Sechub client process has been executed. Time elapsed:" + elapsed + "ms = " + elapsed / 1000 + " seconds");

            ExecutionResult result = extractResult(tmpGoOutputFile, exitCode);

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

    private ProcessBuilder createProcessBuilder(Map<String, String> environmentVariables, File targetFolder, List<String> commandsAsList,
            File tmpGoOutputFile) {
        ProcessBuilder pb = new ProcessBuilder(commandsAsList);
        pb.redirectErrorStream(true);
        pb.redirectOutput(tmpGoOutputFile);

        Map<String, String> environment = pb.environment();
        environment.put("SECHUB_TRUSTALL", "" + trustAll);
        environment.put("SECHUB_INITIAL_WAIT_INTERVAL", "0.1");

        if (IntegrationTestSupport.SECHUB_CLIENT_DEBUGGING_ENABLED) {
            // we enable only when explicit wanted - so logs are smaller and easier to read
            environment.put("SECHUB_DEBUG", "true");
        }
        if (TestUtil.isKeepingTempfiles()) {
            environment.put("SECHUB_KEEP_TEMPFILES", "true");
        }
        if (environmentVariables != null) {
            environment.putAll(environmentVariables);
        }
        pb.directory(targetFolder);
        return pb;
    }

    private ExecutionResult extractResult(File tmpGoOutputFile, int exitCode) {
        String output = TestFileSupport.loadTextFile(tmpGoOutputFile, "\n");
        LOG.info("Command output:\n{}", output);

        /* prepare and return result */
        ExecutionResult result = new ExecutionResult();
        result.lines = output.trim().split("\\n");

        result.exitCode = exitCode;
        result.outputFolder = outputFolder.toFile();
        return result;
    }

    private void logCommand(List<String> commandsAsList) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = commandsAsList.iterator();
        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(" ");
            }
        }
        LOG.info("Execute command '{}'", sb.toString());
    }

    private File createTempFileForOutput() throws IOException {
        /* create temporary file for output */
        File tmpGoOutputFile = TestUtil.createTempFileInBuildFolder("sechub-client-test-", "txt").toFile();
        LOG.info("Temporary go output at:{}", tmpGoOutputFile);
        return tmpGoOutputFile;
    }

    private void assertAPITokenSet(TestUser user) {
        if (user.getApiToken() == null) {
            throw new IllegalStateException("Test user:" + user.getUserId()
                    + " has no apiToken. This can happen if you are using users from another scenario... Please check your test!");
        }
    }

    private void handleAction(ClientAction action, List<String> commandsAsList) {
        if (action != null) {
            commandsAsList.add(action.getCommand());
        }
    }

    private void handleOptions(List<String> commandsAsList, String... options) {
        commandsAsList.addAll(Arrays.asList(options));
    }

    private File resolveScanTargetFolder(File file) {
        File targetFolder = null;

        if (file != null && file.isDirectory()) {
            LOG.info("Handling given file as target folder:{}", file);
            targetFolder = file;
        } else {
            targetFolder = ensureExampleContentFoldersExist();
        }
        return targetFolder;
    }

    private void handleUserCredentials(ApiTokenStrategy apiTokenStrategy, TestUser user, Map<String, String> environmentVariables,
            List<String> commandsAsList) {
        if (user != null) {
            commandsAsList.add("-user");
            commandsAsList.add(user.getUserId());

            switch (apiTokenStrategy) {
            case HIDEN_BY_ENV:
                environmentVariables.put("SECHUB_APITOKEN", user.getApiToken());
                break;
            case VISIBLE_AS_ARGUMENT:
                commandsAsList.add("-apitoken");
                commandsAsList.add(user.getApiToken());
                break;
            default:
                throw new IllegalStateException("User set, but defined unsupported strategy:" + apiTokenStrategy);

            }
        }
    }

    private void handleExecutable(List<String> commandsAsList) {
        File executableFile = resolveExistingExecutableAndAppendAdditionalCommands();
        if (TestUtil.isWindows()) {
            commandsAsList.add("cmd.exe");
            commandsAsList.add("/C");
        }
        commandsAsList.add(executableFile.getAbsolutePath());
    }

    private void handleConfigFileCommand(File file, List<String> commandsAsList) {
        if (file != null) {
            if (file.isFile()) {
                commandsAsList.add("-configfile");
                if (TestUtil.isWindows()) {
                    commandsAsList.add("\"" + file.getAbsolutePath() + "\"");
                } else {
                    commandsAsList.add(file.getAbsolutePath());
                }
            } else if (file.isDirectory()) {
                LOG.info("Handling given file as target folder, so using sechub.json inside:{}", file);
            }
        }
    }

    private File resolveExistingExecutableAndAppendAdditionalCommands() {
        if (cachedExecutableFile != null) {
            return cachedExecutableFile;
        }
        File executableFile = null;

        if (sechubClientBinaryPath == null) {
            String parentFolder = "sechub-cli/build/go/platform/"; // when not set we use build location
            String sechubExeName = "sechub";

            String osArch = SystemUtils.OS_ARCH;
            boolean is64 = osArch.contains("64");
            boolean isArm = osArch.contains("arm") || osArch.contains("aarch");

            if (SystemUtils.IS_OS_WINDOWS && !isArm) {
                sechubExeName += ".exe";
                parentFolder += "windows";
                parentFolder += (is64) ? "-amd64" : "-386";
            } else if (SystemUtils.IS_OS_LINUX) {
                parentFolder += "linux";
                if (isArm) {
                    parentFolder += (is64) ? "-arm64" : "-arm";
                } else {
                    parentFolder += (is64) ? "-amd64" : "-386";
                }
            } else if (SystemUtils.IS_OS_MAC_OSX && is64) {
                parentFolder += "darwin";
                parentFolder += (isArm) ? "-arm64" : "-amd64";
            } else {
                throw new RuntimeException("Unknown OS (" + SystemUtils.OS_NAME + ") or processor architecture (" + osArch + ")");
            }
            File executableParentFolder = new File(IntegrationTestFileSupport.getTestfileSupport().getRootFolder(), parentFolder);
            executableFile = new File(executableParentFolder, sechubExeName);
            LOG.debug("SecHub client path: Calculated: {}", executableFile.getAbsoluteFile());
        } else {
            LOG.debug("SecHub client path: Use defined: {}", sechubClientBinaryPath);
            executableFile = new File(sechubClientBinaryPath);
        }

        if (!executableFile.exists()) {
            throw new SecHubClientNotFoundException(executableFile);
        }
        cachedExecutableFile = executableFile;

        return cachedExecutableFile;
    }

    private File ensureExampleContentFoldersExist() {
        if (cachedExampleContentFolder != null) {
            return cachedExampleContentFolder;
        }
        LOG.debug("Start ensuring example content folders for sechub client executor");
        /*
         * because SecHub client checks for existing folders we must ensure integration
         * test do scan existing folders so we ensure example content/folders exists
         */
        String pathToExamples = "sechub-integrationtest/build/sechub/example/content/"; // same deepness as "sechub-cli/build/go/platform/linux-386"
        File exampleScanRootFolder = new File(IntegrationTestFileSupport.getTestfileSupport().getRootFolder(), pathToExamples);
        exampleScanRootFolder.mkdirs();

        List<IntegrationTestExampleFolder> exampleFolders = IntegrationTestExampleConstants.MOCKDATA_EXAMPLE_CONTENT_PROVIDER.getExampleContentFolders();
        for (IntegrationTestExampleFolder folder : exampleFolders) {
            File projectResourceFolder = new File(exampleScanRootFolder, folder.getPath());
            if (folder.isExistingContent()) {
                assertTrue("This projectResourceFolder must already exist but doesnt:" + projectResourceFolder.getAbsolutePath(),
                        projectResourceFolder.isDirectory() && projectResourceFolder.exists());
            } else {
                projectResourceFolder.mkdirs();// we generate this one
                File testFile1 = new File(projectResourceFolder, "TestMeIfYouCan.java");
                if (!testFile1.exists()) {
                    try {
                        TestFileWriter writer = new TestFileWriter();
                        writer.writeTextToFile("class TestMeifYouCan {}", testFile1, Charset.forName("UTF-8"));
                    } catch (IOException e) {
                        throw new IllegalStateException("Cannot create test output!", e);
                    }

                }
            }
        }
        cachedExampleContentFolder = exampleScanRootFolder;
        return cachedExampleContentFolder;
    }

    public void setTrustAll(boolean trustAll) {
        this.trustAll = trustAll;
    }

    public void setOutputFolder(Path outputFolder) {
        this.outputFolder = outputFolder;
    }

    public void setSechubClientBinaryPath(String pathToSecHubClientBinary) {
        this.sechubClientBinaryPath = pathToSecHubClientBinary;
    }

    public static void main(String[] args) {
        new SecHubClientExecutor().execute(null, ApiTokenStrategy.VISIBLE_AS_ARGUMENT, null, null, null, "-help");
    }

    public class SecHubClientNotFoundException extends IllegalStateException {
        private static final long serialVersionUID = 1L;

        public SecHubClientNotFoundException(File executable) {
            super("SecHub client not available, did you forget to build the client with `gradlew buildGo` ?\nExpected:" + executable);
        }
    }

}
