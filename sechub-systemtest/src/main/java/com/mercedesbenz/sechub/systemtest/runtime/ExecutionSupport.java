package com.mercedesbenz.sechub.systemtest.runtime;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.systemtest.config.ScriptDefinition;

public class ExecutionSupport {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionSupport.class);
    private static final boolean DRY_RUN = Boolean.getBoolean("sechub.systemtest.dryrun");

    private VariableSupport variableSupport;

    public ExecutionSupport(VariableSupport support) {
        this.variableSupport = support;
    }

    public ExecutionResult execute(ScriptDefinition scriptDefinition) throws IOException {
        String scriptPath = scriptDefinition.getPath();
        String workingDirectory = scriptDefinition.getWorkingDirectory();

        LOG.trace("Start:{} inside {}", scriptPath, workingDirectory);

        ExecutionResult result = new ExecutionResult();

        if (DRY_RUN) {
            return result;
        }

        ProcessBuilder pb = new ProcessBuilder(scriptPath);
        pb.inheritIO();
        pb.directory(Paths.get(workingDirectory).toFile());
        pb.environment().putAll(scriptDefinition.getEnvVariables());

        try {
            Process process = pb.start();
            boolean exited = process.waitFor(30, TimeUnit.MINUTES);
            if (!exited) {
                throw new IOException("Time out for execution of command:" + scriptDefinition);
            }
            result.exitValue = process.exitValue();

            /* FIXME Albert Tregnaghi, 2023-03-22: does not work */
            result.errorMessage = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            result.outputMessage = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            LOG.trace("Ended:{} - exit code: {}", scriptDefinition, result.exitValue);

            return result;
        } catch (InterruptedException e) {
            // timed out...
            throw new IOException("Time out for execution of command:" + scriptDefinition);
        }
    }

}
