package com.mercedesbenz.sechub.systemtest.runtime;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.systemtest.config.ScriptDefinition;
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

    public ExecutionResult execute(ScriptDefinition scriptDefinitionWithSecrets) throws IOException {

        ScriptDefinition secretsRevealedScriptDefinition = revealSecretsForScript(scriptDefinitionWithSecrets);

        String scriptPath = secretsRevealedScriptDefinition.getPath();
        String workingDirectory = secretsRevealedScriptDefinition.getWorkingDirectory();

        LOG.trace("Start:{} inside {}", scriptPath, workingDirectory);

        ExecutionResult result = new ExecutionResult();

        if (DRY_RUN) {
            return result;
        }

        ProcessBuilder pb = new ProcessBuilder(scriptPath);
        pb.inheritIO();
        pb.directory(Paths.get(workingDirectory).toFile());
        pb.environment().putAll(secretsRevealedScriptDefinition.getEnvVariables());
        pb.command().addAll(secretsRevealedScriptDefinition.getArguments());

        try {
            Process process = pb.start();
            boolean exited = process.waitFor(30, TimeUnit.MINUTES);
            if (!exited) {
                throw new IOException("Time out for execution of command:" + secretsRevealedScriptDefinition);
            }
            result.exitValue = process.exitValue();

            /* FIXME Albert Tregnaghi, 2023-03-22: does not work */
            result.errorMessage = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            result.outputMessage = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            LOG.trace("Ended:{} - exit code: {}", secretsRevealedScriptDefinition, result.exitValue);

            return result;
        } catch (InterruptedException e) {
            // timed out...
            throw new IOException("Time out for execution of command:" + secretsRevealedScriptDefinition);
        }
    }

    private ScriptDefinition revealSecretsForScript(ScriptDefinition scriptDefinitionWithSecrets) {
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

        return alterableScriptDefinition;
    }

}
