package com.mercedesbenz.sechub.systemtest.runtime;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.systemtest.config.ScriptDefinition;
import com.mercedesbenz.sechub.systemtest.config.TimeUnitDefinition;
import com.mercedesbenz.sechub.systemtest.template.SystemTestTemplateEngine;

public class ExecutionSupport {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionSupport.class);
    private static final boolean DRY_RUN = Boolean.getBoolean("sechub.systemtest.dryrun");

    private EnvironmentProvider environmentProvider;
    private SystemTestTemplateEngine templateEngine;

    public ExecutionSupport(EnvironmentProvider provider) {
        this.environmentProvider = provider;
        this.templateEngine = new SystemTestTemplateEngine();
    }

    public EnvironmentProvider getEnvironmentProvider() {
        return environmentProvider;
    }

    public ProcessContainer execute(ScriptDefinition scriptDefinition) {

        Map<String, String> envVariablesWithSecretsRevealed = revealSecretsForScript(scriptDefinition);

        String scriptPath = scriptDefinition.getPath();
        String workingDirectory = scriptDefinition.getWorkingDirectory();

        LOG.trace("Start:{} inside {}", scriptPath, workingDirectory);

        ProcessContainer processContainer = new ProcessContainer(scriptDefinition);

        if (DRY_RUN) {
            return processContainer;
        }

        ProcessBuilder pb = new ProcessBuilder(scriptPath);
        pb.inheritIO();
        pb.directory(Paths.get(workingDirectory).toFile());
        pb.environment().putAll(envVariablesWithSecretsRevealed);
        pb.command().addAll(scriptDefinition.getArguments());

        Process process;
        try {
            process = pb.start();

            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    try {
                        TimeUnitDefinition timeOut = scriptDefinition.getProcess().getTimeOut();
                        boolean exited = process.waitFor(timeOut.getAmount(), timeOut.getUnit());
                        if (!exited) {
                            LOG.error("Container time out : {} {}", timeOut.getAmount(), timeOut.getUnit());
//                            throw new IOException("Time out for execution of command:" + secretsRevealedScriptDefinition);
                            processContainer.errorMessage = "Time out for execution of command:" + scriptDefinition.getPath();
                            processContainer.exitValue = -1;
                            processContainer.markTimedOut();
                        } else {
                            processContainer.exitValue = process.exitValue();
                            /* FIXME Albert Tregnaghi, 2023-03-22: does not work */
//                            result.errorMessage = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
//                            result.outputMessage = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                        }

                        LOG.trace("Ended:{} - exit code: {}", scriptDefinition, processContainer.exitValue);

                    } catch (InterruptedException e) {
                        // timed out...
                        processContainer.exitValue = -1;
                        processContainer.errorMessage = "Time out for execution of command:" + scriptDefinition;
                    }
                    processContainer.markNoLongerRunning();
                }
            };
            Thread thread = new Thread(runnable, "exec-" + scriptPath);
            thread.start();

        } catch (IOException e) {
            LOG.warn("Script execution failed: {}", e.getMessage());
            processContainer.markProcessNotStartable(e);
        }

        return processContainer;
    }

    private Map<String, String> revealSecretsForScript(ScriptDefinition scriptDefinitionWithSecrets) {
        String withSecretsJson = JSONConverter.get().toJSON(scriptDefinitionWithSecrets);
        ScriptDefinition alterableScriptDefinition = JSONConverter.get().fromJSON(ScriptDefinition.class, withSecretsJson);
        /*
         * at this point we can replace the secret environment parts - they will not be
         * shown inside a dump, because we create an alterableScriptDefinition which is
         * not dumped...
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
