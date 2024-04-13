// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime.launch;

import static java.util.Objects.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.core.util.StacktraceUtil;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.systemtest.config.ScriptDefinition;
import com.mercedesbenz.sechub.systemtest.config.TimeUnitDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.LocationSupport;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestExecutionState;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntimeException;
import com.mercedesbenz.sechub.systemtest.runtime.variable.EnvironmentProvider;
import com.mercedesbenz.sechub.systemtest.runtime.variable.KeepAsIsDynamicVariableCalculator;
import com.mercedesbenz.sechub.systemtest.runtime.variable.VariableCalculator;
import com.mercedesbenz.sechub.systemtest.template.SystemTestTemplateEngine;

public class ExecutionSupport {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionSupport.class);
    private static final KeepAsIsDynamicVariableCalculator NO_CALCULATION = new KeepAsIsDynamicVariableCalculator();

    private EnvironmentProvider environmentProvider;
    private SystemTestTemplateEngine templateEngine;
    private LocationSupport locationSupport;
    private TextFileWriter textFileWriter;
    private TextFileReader textFileReader;
    private AtomicInteger nextProcessContainerNumber = new AtomicInteger(1);

    public ExecutionSupport(EnvironmentProvider provider, LocationSupport locationSupport) {
        requireNonNull(locationSupport);
        requireNonNull(provider);

        this.environmentProvider = provider;
        this.locationSupport = locationSupport;
        this.templateEngine = new SystemTestTemplateEngine();
        this.textFileWriter = new TextFileWriter();
        this.textFileReader = new TextFileReader();
    }

    public EnvironmentProvider getEnvironmentProvider() {
        return environmentProvider;
    }

    public ProcessContainer execute(ScriptDefinition scriptDefinition, SystemTestExecutionState state) {
        return execute(scriptDefinition, null, state);
    }

    public ProcessContainer execute(ScriptDefinition scriptDefinition, VariableCalculator dynamicVariableCalculator, SystemTestExecutionState state) {
        if (dynamicVariableCalculator == null) {
            dynamicVariableCalculator = NO_CALCULATION;
        }
        Map<String, String> envVariables = revealSecretsForScript(scriptDefinition);
        envVariables = dynamicVariableCalculator.calculateEnvironmentEntries(envVariables);

        String scriptPath = scriptDefinition.getPath();
        if (scriptPath == null || scriptPath.isEmpty()) {
            throw new IllegalStateException("Script definition has no script path set!");
        }
        File workingDirectory = resolveWorkingDirectory(scriptDefinition, scriptPath);
        File targetFile = assertScriptCanBeExecuted(scriptPath, workingDirectory);

        ProcessContainer processContainer = new ProcessContainer(nextProcessContainerNumber.getAndIncrement(), scriptDefinition, state, targetFile);

        Path outputFile = locationSupport.ensureOutputFile(processContainer);
        Path errorFile = locationSupport.ensureErrorFile(processContainer);

        writeToFile(processContainer);

        ProcessBuilder pb = new ProcessBuilder(scriptPath);
        if (workingDirectory != null) {
            pb.directory(workingDirectory);
        }
        pb.environment().putAll(envVariables);
        pb.command().addAll(dynamicVariableCalculator.calculateArguments(scriptDefinition.getArguments()));

        pb.redirectOutput(outputFile.toFile());
        pb.redirectError(errorFile.toFile());

        Process process;
        try {
            process = pb.start();
            processContainer.markProcessStarted(process);

            writeToFile(processContainer);

            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    try {
                        TimeUnitDefinition timeOut = scriptDefinition.getProcess().getTimeOut();
                        boolean exited = process.waitFor(timeOut.getAmount(), timeOut.getUnit());
                        if (!exited) {
                            LOG.error("Container timed out : {} {}", timeOut.getAmount(), timeOut.getUnit());
                            processContainer.markTimedOut();

                        } else {
                            processContainer.exitValue = process.exitValue();
                        }

                        LOG.trace("Ended:{} - exit code: {}", scriptDefinition, processContainer.exitValue);

                    } catch (InterruptedException e) {
                        LOG.error("Runable interrupted", e);
                        processContainer.markFailed();
                    } finally {
                        processContainer.markNoLongerRunning();
                        fetchStreamsAndWriteProcessFile(processContainer);
                    }
                }

            };
            Thread thread = new Thread(runnable, "exec-" + scriptPath);
            thread.start();

        } catch (IOException e) {
            LOG.warn("Script execution failed: {}", e.getMessage());
            processContainer.markFailed();

            writeProcessContainerExecutionErrorFile(workingDirectory, processContainer, e);

            fetchStreamsAndWriteProcessFile(processContainer);
        }

        return processContainer;
    }

    private File resolveWorkingDirectory(ScriptDefinition scriptDefinition, String scriptPath) {
        String workingDirectoryAsString = scriptDefinition.getWorkingDirectory();

        File workingDirectory = null;
        if (workingDirectoryAsString != null) {
            workingDirectory = Paths.get(workingDirectoryAsString).toFile();
        } else {
            File scriptFile = new File("./");
            workingDirectory = scriptFile.getParentFile();
        }
        LOG.trace("Start:{} inside {}", scriptPath, workingDirectoryAsString);
        return workingDirectory;
    }

    private File assertScriptCanBeExecuted(String scriptPath, File workingDirectory) {
        File targetFile = new File(workingDirectory, scriptPath);
        String canonicalPath;
        try {
            canonicalPath = targetFile.getCanonicalPath();
        } catch (IOException e) {
            throw new IllegalStateException("The target file path is not a canonical path: " + targetFile.getAbsolutePath(), e);
        }

        if (!targetFile.exists()) {
            throw new IllegalStateException("The target file does not exist:" + canonicalPath);
        }
        try {
            targetFile = targetFile.toPath().toRealPath().toFile();
        } catch (IOException e) {
            throw new IllegalStateException("The target file cannot be converted to real path variant:" + targetFile.getAbsolutePath(), e);
        }
        if (!Files.isExecutable(targetFile.toPath())) {
            throw new IllegalStateException("The target file does exist, but is not executable: " + targetFile.getAbsolutePath());
        }
        return targetFile;
    }

    private void writeProcessContainerExecutionErrorFile(File workingDirectory, ProcessContainer processContainer, IOException reason) {
        Path processContainerErrorFile = locationSupport.ensureProcessContainerErrorFile(processContainer);
        String data = """
                ******************************************************************************
                Execution failed for process container: %s
                ******************************************************************************
                Caller working directory: %s
                Script working directory: %s

                Exception was:
                --------------
                %s

                """.formatted(processContainer.getUuid(), new File("./").getAbsolutePath(), workingDirectory.getAbsolutePath(),
                StacktraceUtil.createFullTraceDescription(reason));
        try {
            textFileWriter.save(processContainerErrorFile.toFile(), data, true);
        } catch (IOException e) {
            LOG.error("Was not able to write error file for process container:{}", processContainer.getUuid(), e);
        }

    }

    private void fetchStreamsAndWriteProcessFile(ProcessContainer processContainer) {
        Path outputFile = locationSupport.ensureOutputFile(processContainer);
        Path errorFile = locationSupport.ensureErrorFile(processContainer);

        try {
            String outputTextLine = textFileReader.loadTextFile(outputFile.toFile(), "\n", 1);
            processContainer.outputMessage = outputTextLine;
        } catch (IOException e) {
            LOG.error("Was not able to read output for process container:{}", processContainer.getUuid(), e);
        }

        try {
            String errorTextLine = textFileReader.loadTextFile(errorFile.toFile(), "\n", 1);
            processContainer.errorMessage = errorTextLine;
        } catch (IOException e) {
            LOG.error("Was not able to read error for process container:{}", processContainer.getUuid(), e);
        }
        writeToFile(processContainer);
    }

    private void writeToFile(ProcessContainer processContainer) {
        Path metaDataFile = locationSupport.ensureProcessContainerFile(processContainer);
        try {
            textFileWriter.save(metaDataFile.toFile(), JSONConverter.get().toJSON(processContainer, true), true);
        } catch (IOException e) {
            throw new SystemTestRuntimeException("Was not able to write file:" + metaDataFile, e);
        }
    }

    private Map<String, String> revealSecretsForScript(ScriptDefinition scriptDefinitionWithSecrets) {
        String withSecretsJson = JSONConverter.get().toJSON(scriptDefinitionWithSecrets);
        ScriptDefinition alterableScriptDefinition = JSONConverter.get().fromJSON(ScriptDefinition.class, withSecretsJson);
        /*
         * at this point we can replace the secret environment parts - they will not be
         * shown inside a dump, because we create an alterableScriptDefinition which is
         * not dumped.
         */
        Map<String, String> scriptEnvVariables = alterableScriptDefinition.getEnvVariables();
        for (String key : scriptEnvVariables.keySet()) {
            String value = scriptEnvVariables.get(key);

            if (!templateEngine.hasSecretEnvironmentVariables(value)) {
                continue;
            }
            String newValue = templateEngine.replaceSecretEnvironmentVariablesWithValues(value, environmentProvider);
            if (templateEngine.hasSecretEnvironmentVariables(newValue)) {
                throw new SystemTestRuntimeException(
                        "Failure for script environment key:" + key + ": Secret environment variables may not contain other secret environment variables!");
            }
            if (templateEngine.hasEnvironmentVariables(newValue)) {
                throw new SystemTestRuntimeException(
                        "Failure for script environment key:" + key + ": Secret environment variables may not contain other environment variables!");
            }
            scriptEnvVariables.put(key, newValue);
        }

        return alterableScriptDefinition.getEnvVariables();

    }

}
