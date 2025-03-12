// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import static com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants.*;
import static com.mercedesbenz.sechub.pds.util.PDSAssert.*;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.OptimisticLockingFailureException;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.commons.pds.ProcessAdapter;
import com.mercedesbenz.sechub.pds.PDSLogConstants;
import com.mercedesbenz.sechub.pds.encryption.PDSEncryptionException;
import com.mercedesbenz.sechub.pds.job.JobConfigurationData;
import com.mercedesbenz.sechub.pds.job.PDSCheckJobStatusService;
import com.mercedesbenz.sechub.pds.job.PDSGetJobStreamService;
import com.mercedesbenz.sechub.pds.job.PDSJobConfiguration;
import com.mercedesbenz.sechub.pds.job.PDSJobConfigurationSupport;
import com.mercedesbenz.sechub.pds.job.PDSJobTransactionService;
import com.mercedesbenz.sechub.pds.job.PDSWorkspacePreparationResult;
import com.mercedesbenz.sechub.pds.job.PDSWorkspaceService;
import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.UseCaseAdminFetchesJobErrorStream;
import com.mercedesbenz.sechub.pds.usecase.UseCaseAdminFetchesJobMetaData;
import com.mercedesbenz.sechub.pds.usecase.UseCaseAdminFetchesJobOutputStream;
import com.mercedesbenz.sechub.pds.usecase.UseCaseSystemExecutesJob;
import com.mercedesbenz.sechub.pds.usecase.UseCaseSystemHandlesJobCancelRequests;
import com.mercedesbenz.sechub.pds.util.PDSResilientRetryExecutor;
import com.mercedesbenz.sechub.pds.util.PDSResilientRetryExecutor.ExceptionThrower;

/**
 * Represents the callable executed inside {@link PDSExecutionFutureTask}
 *
 * @author Albert Tregnaghi
 *
 */
class PDSExecutionCallable implements Callable<PDSExecutionResult> {

    private static final int MAXIMUM_START_TRUNCATE_CHARS = 1024;

    private static final Logger LOG = LoggerFactory.getLogger(PDSExecutionCallable.class);

    private ProcessAdapter process;

    private UUID pdsJobUUID;

    private ExceptionThrower<IllegalStateException> pdsJobUpdateExceptionThrower;

    private PDSMessageCollector messageCollector;

    private PDSJobConfiguration config;

    private boolean cancelOperationsHasBeenStarted;

    private PDSExecutionCallableServiceCollection serviceCollection;

    private boolean addScriptLogToPDSLog;

    public PDSExecutionCallable(UUID pdsJobUUID, PDSExecutionCallableServiceCollection serviceCollection) {
        notNull(pdsJobUUID, "pdsJobUUID may not be null!");
        notNull(serviceCollection, "serviceCollection may not be null!");

        this.pdsJobUUID = pdsJobUUID;
        this.serviceCollection = serviceCollection;

        messageCollector = new PDSMessageCollector();

        pdsJobUpdateExceptionThrower = new ExceptionThrower<IllegalStateException>() {

            @Override
            public void throwException(String message, Exception cause) throws IllegalStateException {
                throw new IllegalStateException("Job execution data refresh failed. " + message, cause);
            }
        };

    }

    @Override
    @UseCaseSystemExecutesJob(@PDSStep(number = 3, name = "PDS execution call", description = "Central point of PDS job execution."))
    public PDSExecutionResult call() throws Exception {
        LOG.info("Prepare execution of PDS job: {}", pdsJobUUID);
        PDSExecutionResult result = new PDSExecutionResult();

        String productPath = null;

        try {
            MDC.clear();
            MDC.put(PDSLogConstants.MDC_PDS_JOB_UUID, Objects.toString(pdsJobUUID));

            getJobTransactionService().markJobAsRunningInOwnTransaction(pdsJobUUID);
            JobConfigurationData data = getJobTransactionService().getJobConfigurationDataOrFail(pdsJobUUID);
            config = data.getJobConfiguration();

            MDC.put(PDSLogConstants.MDC_SECHUB_JOB_UUID, Objects.toString(config.getSechubJobUUID()));

            PDSJobConfigurationSupport configSupport = new PDSJobConfigurationSupport(config);
            addScriptLogToPDSLog = configSupport.isEnabled(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_ADD_SCRIPTLOG_TO_PDSLOG_ENABLED);

            if (addScriptLogToPDSLog) {
                LOG.info("Script log output will be added to PDS logs");
            }

            long minutesToWaitForResult = calculateTimeToWaitForProductInMinutes();

            PDSWorkspacePreparationResult preparationResult = pepareWorkspace(config, data);
            if (preparationResult.isLauncherScriptExecutable()) {

                productPath = getWorkspaceService().getProductPathFor(config);
                createProcess(pdsJobUUID, config, productPath);
                waitForProcessEndAndGetResultByFiles(result, pdsJobUUID, config, minutesToWaitForResult);

            } else {
                LOG.info("Workspace not prepared enough for launcher script, so skipping execution of product: {} for pds job: {}", config.getProductId(),
                        pdsJobUUID);

                result.setExitCode(0);
            }

        } catch (Exception e) {

            LOG.error("Execution of job uuid:{} failed", pdsJobUUID, e);

            result.setFailed(true);
            if (e instanceof PDSEncryptionException) {
                result.setEncryptionFailure(true);
            }
            result.setResult("Execution of job uuid:" + pdsJobUUID + " failed. Please look into PDS logs for details and search for former string.");

        } finally {
            cleanUpWorkspace(pdsJobUUID, config);

            MDC.clear();
        }

        /*
         * handle always exit code. Everything having an exit code != 0 is handled as an
         * error
         */
        if (result.getExitCode() != 0) {
            result.setFailed(true);
        }
        result.setCanceled(cancelOperationsHasBeenStarted);

        LOG.info("Finished execution of job {} with exitCode={}, failed={}, cancelOperationsHasBeenStarted={}", pdsJobUUID, result.getExitCode(),
                result.isFailed(), cancelOperationsHasBeenStarted);

        if (result.isFailed()) {
            PDSGetJobStreamService pdsGetJobStreamService = serviceCollection.getPdsGetJobStreamService();
            String truncatedErrorStream = pdsGetJobStreamService.getJobErrorStreamTruncated(pdsJobUUID);
            String truncatedOutputStream = pdsGetJobStreamService.getJobOutputStreamTruncated(pdsJobUUID);

            int lastChars = PDSGetJobStreamService.TRUNCATED_STREAM_SIZE;
            String message = """
                    Execution of PDS job %s failed!

                    Product path: %s
                    Exit code   : %d

                    Job error stream (last %s chars):
                    ------------------------------------
                    %s

                    Job output stream (last %s chars):
                    ------------------------------------
                    %s

                    """.formatted(pdsJobUUID, productPath, result.getExitCode(), lastChars, truncatedErrorStream, lastChars, truncatedOutputStream);

            LOG.error(message);
        }

        return result;
    }

    public UUID getPdsJobUUID() {
        return pdsJobUUID;
    }

    private long calculateTimeToWaitForProductInMinutes() {
        ProcessHandlingDataFactory processHandlingDataFactory = serviceCollection.getProcessHandlingDataFactory();
        ProductLaunchProcessHandlingData launchProcessdata = processHandlingDataFactory.createForLaunchOperation(config);

        int minutesToWaitForResult = launchProcessdata.getMinutesToWaitBeforeProductTimeout();
        if (minutesToWaitForResult < 1) {
            throw new IllegalStateException("The time in minutes to wait for a product is too low:" + minutesToWaitForResult);
        }
        return minutesToWaitForResult;
    }

    private PDSWorkspacePreparationResult pepareWorkspace(PDSJobConfiguration config, JobConfigurationData data) throws IOException {
        LOG.debug("Start workspace preparation for PDS job: {}", pdsJobUUID);

        PDSWorkspaceService workspaceService = getWorkspaceService();
        PDSWorkspacePreparationResult result = workspaceService.prepare(pdsJobUUID, config, data.getMetaData());

        LOG.debug("Workspace preparation done for PDS job: {} - result: {}", pdsJobUUID, result);
        return result;
    }

    void waitForProcessEndAndGetResultByFiles(PDSExecutionResult result, UUID jobUUID, PDSJobConfiguration config, long minutesToWaitForResult)
            throws InterruptedException, IOException {

        /* watching */
        String watcherThreadName = "PDSJob:" + pdsJobUUID + "-stream-watcher";

        LOG.debug("Start watcher thread: {}", watcherThreadName);

        StreamDataRefreshRequestWatcherRunnable streamDatawatcherRunnable = new StreamDataRefreshRequestWatcherRunnable(pdsJobUUID);
        Thread streamDataWatcherThread = new Thread(streamDatawatcherRunnable);
        streamDataWatcherThread.setName(watcherThreadName);
        streamDataWatcherThread.start();

        /* waiting for process */
        LOG.debug("Wait for process of job with uuid:{}, will wait {} minutes for result from product with id:{}", jobUUID, minutesToWaitForResult,
                config.getProductId());
        long started = System.currentTimeMillis();

        boolean exitDoneInTime = process.waitFor(minutesToWaitForResult, TimeUnit.MINUTES);
        long timeElapsedInMilliseconds = System.currentTimeMillis() - started;

        if (exitDoneInTime) {
            LOG.debug("Job execution {} done - product id:{}.", jobUUID, config.getProductId());

            result.setFailed(false);
            result.setExitCode(process.exitValue());

            LOG.debug("Process of PDS job with uuid: {} ended in time with exit code: {} after {} ms - for product with id: {}", jobUUID, result.getExitCode(),
                    timeElapsedInMilliseconds, config.getProductId());

            storeResultFileOrCreateShrinkedProblemDataInstead(result, jobUUID);

        } else {
            LOG.error("Process did not end in time for PDS job with uuid: {} for product id: {}. Waited {} minutes.", jobUUID, config.getProductId(),
                    minutesToWaitForResult);

            result.setFailed(true);
            result.setResult("Product time out.");
            result.setExitCode(1);

            prepareForCancel(true); // here we just reuse cancel operation to ensure process will be terminated same
                                    // way as done for canceling
        }

        streamDatawatcherRunnable.stop();

        writeJobExecutionDataToDatabase(jobUUID, addScriptLogToPDSLog);
        writeProductMessagesToDatabaseWhenMessagesFound(jobUUID);

    }

    private void storeResultFileOrCreateShrinkedProblemDataInstead(PDSExecutionResult result, UUID jobUUID) throws IOException {
        PDSWorkspaceService workspaceService = getWorkspaceService();

        File file = workspaceService.getResultFile(jobUUID);
        String encoding = workspaceService.getFileEncoding(jobUUID);

        if (file.exists()) {
            LOG.debug("Result file found - will read data and set as result");

            result.setResult(FileUtils.readFileToString(file, encoding));
        } else {
            LOG.debug("Result file NOT found - will append output and error streams as result");

            result.setFailed(true);
            result.setResult("Result file not found at " + file.getAbsolutePath());

            int max = MAXIMUM_START_TRUNCATE_CHARS;

            String shrinkedStartOfOutputStream = appendOutputStreamToResultAndReturnShrinkedVariant(result, jobUUID, encoding, max);
            String shrinkedStartOfErrorStream = appendErrorStreamToResultAndReturnShrinkedVariant(result, jobUUID, encoding, max);

            String message = """
                    Execution of PDS job %s created no result file!

                    Job error stream (first %d chars):
                    ---------------------------------------
                    %s

                    Job output stream (first %d chars):
                    ----------------------------------------
                    %s

                    """.formatted(pdsJobUUID, max, shrinkedStartOfErrorStream, max, shrinkedStartOfOutputStream);

            LOG.error(message);

        }

    }

    private String appendErrorStreamToResultAndReturnShrinkedVariant(PDSExecutionResult result, UUID jobUUID, String encoding, int max) throws IOException {
        String shrinkedErrorStream = null;
        File systemErrorFile = getWorkspaceService().getSystemErrorFile(jobUUID);
        if (systemErrorFile.exists()) {
            String error = FileUtils.readFileToString(systemErrorFile, encoding);
            result.setResult(result.getResult() + "\nErrors:\n" + error);
            shrinkedErrorStream = shrinkTo(error, max);
        }
        return shrinkedErrorStream;
    }

    private String appendOutputStreamToResultAndReturnShrinkedVariant(PDSExecutionResult result, UUID jobUUID, String encoding, int max) throws IOException {
        File systemOutFile = getWorkspaceService().getSystemOutFile(jobUUID);
        String shrinkedOutputStream = null;

        if (systemOutFile.exists()) {
            String output = FileUtils.readFileToString(systemOutFile, encoding);
            result.setResult(result.getResult() + "\nOutput:\n" + output);
            shrinkedOutputStream = shrinkTo(output, max);
        }
        return shrinkedOutputStream;
    }

    private void writeProductMessagesToDatabaseWhenMessagesFound(UUID pdsJobUUID) {
        LOG.debug("Collect messages for pds job: {}", pdsJobUUID);
        SecHubMessagesList messages = readProductMessages(pdsJobUUID);

        if (messages.getSecHubMessages().isEmpty()) {
            LOG.debug("No messages for pds job: {} found. So skip database access.", pdsJobUUID);
            return;
        }

        LOG.debug("Writing messages to database for job:{}", pdsJobUUID);
        PDSResilientRetryExecutor<IllegalStateException> executor = new PDSResilientRetryExecutor<>(3, pdsJobUpdateExceptionThrower,
                OptimisticLockingFailureException.class);
        executor.execute(() -> {
            getJobTransactionService().updateJobMessagesInOwnTransaction(pdsJobUUID, messages);
        }, pdsJobUUID.toString());

    }

    @UseCaseAdminFetchesJobOutputStream(@PDSStep(name = "Update ouptut stream data", description = "Reads output stream data from workspace files and stores content inside database. Will also refresh update time stamp for caching mechanism.", number = 4))
    @UseCaseAdminFetchesJobErrorStream(@PDSStep(name = "Update error stream data", description = "Reads error stream data from workspace files and stores content inside database. Will also refresh update time stamp for caching mechanism.", number = 4))
    @UseCaseAdminFetchesJobMetaData(@PDSStep(name = "Update meta data", description = "Reads meta data from workspace file and stores content inside database if not null. Will also refresh update time stamp for caching mechanism.", number = 4))
    private void writeJobExecutionDataToDatabase(UUID pdsJobUUID, boolean nowAddScriptLogToPDSLog) {
        LOG.debug("Writing job execution data to database for pds job:{}", pdsJobUUID);

        final PDSExecutionData executionData = readJobExecutionData(pdsJobUUID);

        PDSResilientRetryExecutor<IllegalStateException> executor = new PDSResilientRetryExecutor<>(3, pdsJobUpdateExceptionThrower,
                OptimisticLockingFailureException.class);
        executor.execute(() -> {
            getJobTransactionService().updateJobExecutionDataInOwnTransaction(pdsJobUUID, executionData);
            return null;
        }, pdsJobUUID.toString());

        if (!nowAddScriptLogToPDSLog) {
            return;
        }
        String output = """
                Added launcher script log output to PDS log:\n
                *******************************************************************************************************************
                Script output of PDS job: {}
                *******************************************************************************************************************
                Output stream:

                {}

                Error stream:

                {}

                *******************************************************************************************************************
                END OF Stream data from launcher script of pds job: {},
                *******************************************************************************************************************
                """;
        LOG.info(output, pdsJobUUID, executionData.getOutputStreamData(), executionData.getErrorStreamData(), pdsJobUUID);

    }

    private SecHubMessagesList readProductMessages(UUID pdsJobUUID) {
        File productMessagesFolder = getWorkspaceService().getMessagesFolder(pdsJobUUID);

        List<SecHubMessage> collected = messageCollector.collect(productMessagesFolder);

        return new SecHubMessagesList(collected);
    }

    private PDSExecutionData readJobExecutionData(UUID pdsJobUUID) {
        String encoding = getWorkspaceService().getFileEncoding(pdsJobUUID);

        PDSExecutionData executionData = new PDSExecutionData();

        readOutputStream(pdsJobUUID, encoding, executionData);
        readErrorStream(pdsJobUUID, encoding, executionData);
        readMetaData(pdsJobUUID, encoding, executionData);

        return executionData;
    }

    private void readMetaData(UUID pdsJobUUID, String encoding, PDSExecutionData executionData) {
        /* handle meta data file */
        File metaDataFile = getWorkspaceService().getMetaDataFile(pdsJobUUID);
        if (!metaDataFile.exists()) {
            return;
        }
        try {
            executionData.metaData = FileUtils.readFileToString(metaDataFile, encoding);
        } catch (IOException e) {
            LOG.error("Was not able to fetch meta data for PDS job:{} on path:{}!", pdsJobUUID, metaDataFile.getAbsolutePath(), e);
        }

    }

    private void readErrorStream(UUID pdsJobUUID, String encoding, PDSExecutionData executionData) {
        /* handle error stream */
        File systemErrorFile = getWorkspaceService().getSystemErrorFile(pdsJobUUID);
        if (!systemErrorFile.exists()) {
            return;
        }
        try {
            executionData.errorStreamData = FileUtils.readFileToString(systemErrorFile, encoding);
        } catch (IOException e) {
            LOG.error("Was not able to fetch error stream data for PDS job:{} on path:{}!", pdsJobUUID, systemErrorFile.getAbsolutePath(), e);
        }
    }

    private void readOutputStream(UUID pdsJobUUID, String encoding, PDSExecutionData executionData) {
        /* handle output stream */
        File systemOutFile = getWorkspaceService().getSystemOutFile(pdsJobUUID);
        if (!systemOutFile.exists()) {
            return;
        }
        try {
            executionData.outputStreamData = FileUtils.readFileToString(systemOutFile, encoding);
        } catch (IOException e) {
            LOG.error("Was not able to fetch output stream data for PDS job:{} on path:{}!", pdsJobUUID, systemOutFile.getAbsolutePath(), e);
        }
    }

    private String shrinkTo(String content, int max) {
        if (content == null) {
            return null;
        }
        if (content.length() < max) {
            return content;
        }
        return content.substring(0, max - 3) + "...";
    }

    private void createProcess(UUID pdsJobUUID, PDSJobConfiguration config, String path) throws IOException {
        if (path == null) {
            throw new IllegalStateException("Path not defined for product id:" + config.getProductId());
        }

        File currentDir = Paths.get("./").toRealPath().toFile();
        List<String> commands = new ArrayList<>();
        commands.add(path);

        PDSWorkspaceService workspaceService = getWorkspaceService();
        ProcessBuilder builder = new ProcessBuilder(commands);

        builder.directory(currentDir);
        builder.redirectInput(Redirect.INHERIT);
        builder.redirectOutput(workspaceService.getSystemOutFile(pdsJobUUID));
        builder.redirectError(workspaceService.getSystemErrorFile(pdsJobUUID));

        /*
         * add parts from PDS job configuration - means data defined by caller before
         * job was marked as ready to start
         */

        PDSExecutionEnvironmentService environmentService = getEnvironmentService();

        environmentService.initProcessBuilderEnvironmentMap(pdsJobUUID, config, builder);

        LOG.info("Start launcher script for pds job: {} from path: {}", pdsJobUUID, path);
        try {

            process = serviceCollection.getProcessAdapterFactory().startProcess(builder);

        } catch (IOException e) {
            LOG.error("Process start failed for pdsJobUUID:{}. Current directory was:{}", pdsJobUUID, currentDir.getAbsolutePath());
            throw e;
        }
    }

    /**
     * Is called before cancel operation on caller (user) side or when time out of
     * execution has been reached!
     *
     * @param mayInterruptIfRunning
     * @return <code>true</code> when the process has terminated itself or a hard
     *         termination was done. <code>false</code> when process cancellation
     *         failed
     */
    @UseCaseSystemHandlesJobCancelRequests(@PDSStep(name = "process cancellation", description = "process created by job will be destroyed. If configured, a given time in seconds will be waited, to give the process the chance handle some cleanup and to end itself.", number = 4))
    boolean prepareForCancel(boolean mayInterruptIfRunning) {
        LOG.info("Prepare cancel of PDS job: {}: starting", pdsJobUUID);

        if (process == null) {
            LOG.info("Skip cancellation of PDS job {} because process was null.", pdsJobUUID);
            return true;
        }

        if (!process.isAlive()) {
            LOG.info("Skip cancellation of PDS job {} because process already no longer alive.", pdsJobUUID);
            return true;
        }

        cancelOperationsHasBeenStarted = true;

        ProductCancellationProcessHandlingData processHandlingData = null;

        if (config == null) {
            LOG.warn("No configuration available for job: {}. Cannot create dedicated process handling data. No process wait possible.", pdsJobUUID);
        } else {
            ProcessHandlingDataFactory processHandlingDataFactory = serviceCollection.getProcessHandlingDataFactory();
            processHandlingData = processHandlingDataFactory.createForCancelOperation(config);
        }

        try {
            handleProcessCancellation(processHandlingData);
            return true;
        } catch (RuntimeException e) {
            return false;
        } finally {
            PDSJobConfiguration jobConfiguration = null;

            try {
                JobConfigurationData data = getJobTransactionService().getJobConfigurationDataOrFail(pdsJobUUID);
                jobConfiguration = data.getJobConfiguration();

            } catch (PDSEncryptionException e) {
                LOG.warn("Was not able to decrypt configuration of PDS job: {}", pdsJobUUID, e);

                LOG.info("Create fallback configuration, asuming sechub storage reuse is enabled (SecHub does storage cleanup)");
                jobConfiguration = new PDSJobConfiguration();
                PDSExecutionParameterEntry resueSecHubConfigParameter = new PDSExecutionParameterEntry(PARAM_KEY_PDS_CONFIG_USE_SECHUB_STORAGE, "true");
                jobConfiguration.getParameters().add(resueSecHubConfigParameter);
            }
            cleanUpWorkspace(pdsJobUUID, jobConfiguration);

        }

    }

    private void handleProcessCancellation(ProductCancellationProcessHandlingData processHandlingData) {

        if (isWaitOnCancelOperationAccepted(processHandlingData)) {
            int millisecondsToWaitForNextCheck = processHandlingData.getMillisecondsToWaitForNextCheck();
            LOG.info("Cancel job: {}: give process chance to cancel. Will wait a maximum time of {} seconds. The check intervall is: {} milliseconds",
                    pdsJobUUID, processHandlingData.getSecondsToWaitForProcess(), millisecondsToWaitForNextCheck);

            while (processHandlingData.isStillWaitingForProcessAccepted()) {
                if (process.isAlive()) {
                    try {
                        LOG.debug("Cancel PDS job: {}: wait {} milliseconds before next process alive check.", pdsJobUUID, millisecondsToWaitForNextCheck);
                        Thread.sleep(millisecondsToWaitForNextCheck);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    LOG.info("Cancel PDS job: {}: process is no longer alive", pdsJobUUID);
                    break;
                }
            }
            LOG.info("Cancel PDS job: {}: waited {} milliseconds at all", pdsJobUUID,
                    System.currentTimeMillis() - processHandlingData.getProcessStartTimeStamp());

        } else {
            LOG.info("Cancel PDS job: {}: will not wait.", pdsJobUUID);
        }

        /* Ensure process is terminated */
        if (process.isAlive()) {
            LOG.info("Cancel PDS job: {}: still alive, will destroy underlying process forcibly.", pdsJobUUID);
            process.destroyForcibly(); // SIGKILL by JVM
        } else {
            LOG.info("Cancel PDS job: {}: has terminated itself.", pdsJobUUID);
        }
    }

    private boolean isWaitOnCancelOperationAccepted(ProductCancellationProcessHandlingData processHandlingData) {
        if (processHandlingData == null) {
            LOG.debug("Not waiting because no process handling data!");
            return false;
        }
        return processHandlingData.isStillWaitingForProcessAccepted();
    }

    private void cleanUpWorkspace(UUID pdsJobUUID, PDSJobConfiguration config) {
        if (getWorkspaceService().isWorkspaceAutoCleanDisabled()) {
            LOG.info("Auto cleanup is disabled, so keep files at {}", getWorkspaceService().getWorkspaceFolder(pdsJobUUID));
            return;
        }
        try {
            getWorkspaceService().cleanup(pdsJobUUID, config);
            LOG.debug("workspace cleanup done for job:{}", pdsJobUUID);
        } catch (IOException e) {
            LOG.error("workspace cleanup failed for job:{}!", pdsJobUUID);
        }
    }

    private PDSJobTransactionService getJobTransactionService() {
        return serviceCollection.getJobTransactionService();
    }

    private PDSExecutionEnvironmentService getEnvironmentService() {
        return serviceCollection.getEnvironmentService();
    }

    private PDSCheckJobStatusService getJobStatusService() {
        return serviceCollection.getJobStatusService();
    }

    private PDSWorkspaceService getWorkspaceService() {
        return serviceCollection.getWorkspaceService();
    }

    private class StreamDataRefreshRequestWatcherRunnable implements Runnable {

        private boolean stopped;
        private UUID jobUUID;

        private StreamDataRefreshRequestWatcherRunnable(UUID jobUUID) {
            this.jobUUID = jobUUID;
        }

        @Override
        public void run() {
            while (!stopped && !Thread.currentThread().isInterrupted()) {
                LOG.trace("start checking stream data refresh requests");

                if (getJobStatusService().isJobStreamUpdateNecessary(jobUUID)) {
                    writeJobExecutionDataToDatabase(jobUUID, false);
                }
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