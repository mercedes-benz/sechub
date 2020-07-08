package com.daimler.sechub.pds.execution;

import static com.daimler.sechub.pds.util.PDSAssert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.pds.job.PDSJobConfiguration;
import com.daimler.sechub.pds.job.PDSJobTransactionService;
import com.daimler.sechub.pds.job.PDSWorkspaceService;
/**
 * Represents the callable executed inside {@link PDSExecutionFutureTask}
 * 
 * @author Albert Tregnaghi
 *
 */
class PDSExecutionCallable implements Callable<PDSExecutionResult> {

    private static final Logger LOG = LoggerFactory.getLogger(PDSExecutionCallable.class);

    private PDSJobTransactionService jobTransactionService;
    private Process process;

    private PDSWorkspaceService workspaceService;

    private PDSExecutionEnvironmentService environmentService;

    private UUID jobUUID;

    public PDSExecutionCallable(UUID jobUUID, PDSJobTransactionService jobTransactionService, PDSWorkspaceService workspaceService,
            PDSExecutionEnvironmentService environmentService) {
        notNull(jobUUID, "jobUUID may not be null!");
        notNull(jobUUID, "jobUUID may not be null!");
        this.jobUUID = jobUUID;
        this.jobTransactionService = jobTransactionService;
        this.workspaceService = workspaceService;
        this.environmentService = environmentService;
    }

    @Override
    public PDSExecutionResult call() throws Exception {
        PDSExecutionResult result = new PDSExecutionResult();
        try {
            String configJSON = jobTransactionService.getJobConfiguration(jobUUID);

            PDSJobConfiguration config = PDSJobConfiguration.fromJSON(configJSON);
            long minutesToWaitForResult = workspaceService.getMinutesToWaitForResult(config);
            if (minutesToWaitForResult < 1) {
                throw new IllegalStateException("Minutes to wait for result configured too low:" + minutesToWaitForResult);
            }

            workspaceService.unzipUploadsWhenConfigured(jobUUID, config);
            String path = workspaceService.getProductPathFor(config);
            if (path == null) {
                throw new IllegalStateException("Path not defined for product id:" + config.getProductId());
            }

            createProcess(jobUUID, config, path);

            waitForProcessEndAndGetResultByFiles(result, jobUUID, config, minutesToWaitForResult);

        } catch (Exception e) {
            LOG.error("Execution of job uuid:{} failed", jobUUID, e);
            result.failed = true;
            result.result = "Execution of job uuid:" + jobUUID + " failed. Please look into PDS logs for details and search for former string.";
        } finally {
            cleanUpWorkspace(jobUUID);
        }
        return result;
    }

    private void waitForProcessEndAndGetResultByFiles(PDSExecutionResult result, UUID jobUUID, PDSJobConfiguration config, long minutesToWaitForResult)
            throws InterruptedException, IOException {
        boolean exitDoneInTime = process.waitFor(minutesToWaitForResult, TimeUnit.MINUTES);

        if (!exitDoneInTime) {
            LOG.error("Job {} failed - product id:{} time out reached.", jobUUID, config.getProductId());
            result.failed = true;
            result.result = "Product time out.";
            result.exitCode = 1;
            prepareForCancel(true);
            return;
        }

        result.exitCode = process.exitValue();
        result.result = "";
        File file = workspaceService.getResultFile(jobUUID);
        String encoding = workspaceService.getFileEncoding(jobUUID);
        if (file.exists()) {
            result.result = FileUtils.readFileToString(file, encoding);
        } else {
            /*
             * no result file available - so snap error and system out and paste as result
             */
            result.failed = true;
            result.result = "Result file not found at " + file.getAbsolutePath();

            File systemOutFile = workspaceService.getSystemOutFile(jobUUID);
            if (systemOutFile.exists()) {
                String error = FileUtils.readFileToString(systemOutFile, encoding);
                result.result += "\nOutput:\n" + error;
            }

            File systemErrorFile = workspaceService.getSystemErrorFile(jobUUID);
            if (systemErrorFile.exists()) {
                String error = FileUtils.readFileToString(systemErrorFile, encoding);
                result.result += "\nErrors:\n" + error;
            }
        }
    }

    private void createProcess(UUID jobUUID, PDSJobConfiguration config, String path) throws IOException {
        File currentDir = Paths.get("./").toRealPath().toFile();
        List<String> commands = new ArrayList<>();
        commands.add(path);

        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.directory(currentDir);
        builder.inheritIO();

        builder.redirectError(workspaceService.getSystemErrorFile(jobUUID));
        builder.redirectOutput(workspaceService.getSystemOutFile(jobUUID));

        builder.environment().putAll(environmentService.buildEnvironmentMap(config));
        File workspaceFolder = workspaceService.getWorkspaceFolder(jobUUID);
        builder.environment().put("PDS_JOB_WORKSPACE_LOCATION", workspaceFolder.toPath().toRealPath().toString());
        try {
            process = builder.start();
        } catch (IOException e) {
            LOG.error("Process start failed for jobUUID:{}. Current directory was:{}", jobUUID, currentDir.getAbsolutePath());
            throw e;
        }
    }

    /**
     * Is called before cancel operation on caller / task side
     * 
     * @param mayInterruptIfRunning
     */
    void prepareForCancel(boolean mayInterruptIfRunning) {
        if (process == null || !process.isAlive()) {
            return;
        }
        try {
            LOG.info("Cancelation of process: {} will destroy underlying process forcibly");
            process.destroyForcibly();
        } finally {
            cleanUpWorkspace(jobUUID);
        }

    }

    private void cleanUpWorkspace(UUID jobUUID) {
        if (workspaceService.isWorkspaceAutoCleanDisabled()) {
            LOG.info("Auto cleanup is disabled, so keep files at {}", workspaceService.getWorkspaceFolder(jobUUID));
            return;
        }
        try {
            workspaceService.cleanup(jobUUID);
        } catch (IOException e) {
            LOG.error("workspace cleanup failed for job:{}!", jobUUID);
        }
    }

}