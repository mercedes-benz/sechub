// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import static com.mercedesbenz.sechub.commons.pds.PDSLauncherScriptEnvironmentConstants.*;
import static com.mercedesbenz.sechub.pds.util.PDSAssert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.mercedesbenz.sechub.pds.PDSJSONConverterException;
import com.mercedesbenz.sechub.pds.PDSLogConstants;
import com.mercedesbenz.sechub.pds.job.JobConfigurationData;
import com.mercedesbenz.sechub.pds.job.PDSCheckJobStatusService;
import com.mercedesbenz.sechub.pds.job.PDSJobConfiguration;
import com.mercedesbenz.sechub.pds.job.PDSJobTransactionService;
import com.mercedesbenz.sechub.pds.job.PDSWorkspaceService;
import com.mercedesbenz.sechub.pds.job.WorkspaceLocationData;
import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.UseCaseAdminFetchesJobErrorStream;
import com.mercedesbenz.sechub.pds.usecase.UseCaseAdminFetchesJobMetaData;
import com.mercedesbenz.sechub.pds.usecase.UseCaseAdminFetchesJobOutputStream;
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

    private static final Logger LOG = LoggerFactory.getLogger(PDSExecutionCallable.class);

    private PDSJobTransactionService jobTransactionService;
    private ProcessAdapter process;

    private PDSWorkspaceService workspaceService;

    private PDSExecutionEnvironmentService environmentService;

    private UUID pdsJobUUID;

    private PDSCheckJobStatusService jobStatusService;

    private ExceptionThrower<IllegalStateException> pdsJobUpdateExceptionThrower;

    private PDSMessageCollector messageCollector;

    private ProcessHandlingDataFactory handlingDataFactory;

    private PDSJobConfiguration config;

    private boolean cancelOperationsHasBeenStarted;

    private PDSProcessAdapterFactory processAdapterFactory;

    public PDSExecutionCallable(UUID jobUUID, PDSJobTransactionService jobTransactionService, PDSWorkspaceService workspaceService,
            PDSExecutionEnvironmentService environmentService, PDSCheckJobStatusService jobStatusService, PDSProcessAdapterFactory processAdapterFactory) {
        notNull(jobUUID, "pdsJobUUID may not be null!");
        notNull(jobTransactionService, "jobTransactionService may not be null!");
        notNull(workspaceService, "workspaceService may not be null!");
        notNull(jobStatusService, "jobStatusService may not be null!");

        this.pdsJobUUID = jobUUID;
        this.jobTransactionService = jobTransactionService;
        this.workspaceService = workspaceService;
        this.environmentService = environmentService;
        this.jobStatusService = jobStatusService;
        this.processAdapterFactory = processAdapterFactory;

        messageCollector = new PDSMessageCollector();
        handlingDataFactory = new ProcessHandlingDataFactory();

        pdsJobUpdateExceptionThrower = new ExceptionThrower<IllegalStateException>() {

            @Override
            public void throwException(String message, Exception cause) throws IllegalStateException {
                throw new IllegalStateException("Job execution data refresh failed. " + message, cause);
            }
        };

    }

    @Override
    public PDSExecutionResult call() throws Exception {
        LOG.info("Prepare execution of PDS job {}", pdsJobUUID);
        PDSExecutionResult result = new PDSExecutionResult();
        try {
            MDC.clear();
            MDC.put(PDSLogConstants.MDC_PDS_JOB_UUID, Objects.toString(pdsJobUUID));

            jobTransactionService.markJobAsRunningInOwnTransaction(pdsJobUUID);

            JobConfigurationData data = jobTransactionService.getJobConfigurationData(pdsJobUUID);
            config = PDSJobConfiguration.fromJSON(data.getJobConfigurationJson());

            MDC.put(PDSLogConstants.MDC_SECHUB_JOB_UUID, Objects.toString(config.getSechubJobUUID()));

            long minutesToWaitForResult = assertMinutesToWaitForResult(config);

            pepareWorkspace(config, data);
            createProcess(pdsJobUUID, config, workspaceService.getProductPathFor(config));
            waitForProcessEndAndGetResultByFiles(result, pdsJobUUID, config, minutesToWaitForResult);

        } catch (Exception e) {

            LOG.error("Execution of job uuid:{} failed", pdsJobUUID, e);

            result.failed = true;
            result.result = "Execution of job uuid:" + pdsJobUUID + " failed. Please look into PDS logs for details and search for former string.";

        } finally {
            cleanUpWorkspace(pdsJobUUID, config);

            MDC.clear();
        }

        /*
         * handle always exit code. Everything having an exit code != 0 is handled as an
         * error
         */
        if (result.exitCode != 0) {
            result.failed = true;
        }
        result.canceled = cancelOperationsHasBeenStarted;

        LOG.info("Finished execution of job {} with exitCode={}, failed={}, cancelOperationsHasBeenStarted={}", pdsJobUUID, result.exitCode, result.failed,
                cancelOperationsHasBeenStarted);

        return result;
    }

    private long assertMinutesToWaitForResult(PDSJobConfiguration config) {
        long minutesToWaitForResult = workspaceService.getMinutesToWaitForResult(config);
        if (minutesToWaitForResult < 1) {
            throw new IllegalStateException("Minutes to wait for result configured too low:" + minutesToWaitForResult);
        }
        return minutesToWaitForResult;
    }

    private void pepareWorkspace(PDSJobConfiguration config, JobConfigurationData data) throws IOException {
        LOG.debug("Start workspace preparation for PDS job: {}", pdsJobUUID);

        workspaceService.prepareWorkspace(pdsJobUUID, config, data.getMetaData());
        workspaceService.extractZipFileUploadsWhenConfigured(pdsJobUUID, config);
        workspaceService.extractTarFileUploadsWhenConfigured(pdsJobUUID, config);

        LOG.debug("Workspace preparation done for PDS job: {}", pdsJobUUID);
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

            result.failed = false;
            result.exitCode = process.exitValue();

            LOG.debug("Process of job with uuid:{} ended after with exit code: {} after {} ms - for product with id: {}", jobUUID, result.exitCode,
                    timeElapsedInMilliseconds, config.getProductId());

            storeResultFileOrCreateShrinkedProblemDataInstead(result, jobUUID);

        } else {
            LOG.error("Job execution {} failed - product id:{} time out reached.", jobUUID, config.getProductId());

            result.failed = true;
            result.result = "Product time out.";
            result.exitCode = 1;

            prepareForCancel(true);
        }

        streamDatawatcherRunnable.stop();

        writeJobExecutionDataToDatabase(jobUUID);
        writeProductMessagesToDatabaseWhenMessagesFound(jobUUID);

    }

    private void storeResultFileOrCreateShrinkedProblemDataInstead(PDSExecutionResult result, UUID jobUUID) throws IOException {
        File file = workspaceService.getResultFile(jobUUID);
        String encoding = workspaceService.getFileEncoding(jobUUID);

        if (file.exists()) {
            LOG.debug("Result file found - will read data and set as result");
            result.result = FileUtils.readFileToString(file, encoding);
        } else {
            LOG.debug("Result file NOT found - will fetch system out and and err and use this as to set result");
            /*
             * no result file available - so snap error and system out and paste as result
             */
            result.failed = true;
            result.result = "Result file not found at " + file.getAbsolutePath();

            String shrinkedOutputStream = createShrinkedOutput(result, jobUUID, encoding);
            String shrinkedErrorStream = createShrinkedError(result, jobUUID, encoding);

            LOG.error("job {} wrote no result file - here part of console log:\noutput stream:\n{}\nerror stream:\n{}", jobUUID, shrinkedOutputStream,
                    shrinkedErrorStream);

        }

    }

    private String createShrinkedError(PDSExecutionResult result, UUID jobUUID, String encoding) throws IOException {
        String shrinkedErrorStream = null;
        File systemErrorFile = workspaceService.getSystemErrorFile(jobUUID);
        if (systemErrorFile.exists()) {
            String error = FileUtils.readFileToString(systemErrorFile, encoding);
            result.result += "\nErrors:\n" + error;
            shrinkedErrorStream = maximum1024chars(error);
        }
        return shrinkedErrorStream;
    }

    private String createShrinkedOutput(PDSExecutionResult result, UUID jobUUID, String encoding) throws IOException {
        File systemOutFile = workspaceService.getSystemOutFile(jobUUID);
        String shrinkedOutputStream = null;

        if (systemOutFile.exists()) {
            String output = FileUtils.readFileToString(systemOutFile, encoding);
            result.result += "\nOutput:\n" + output;
            shrinkedOutputStream = maximum1024chars(output);
        }
        return shrinkedOutputStream;
    }

    private void writeProductMessagesToDatabaseWhenMessagesFound(UUID jobUUID) {
        LOG.debug("Collect messages for job:{}", jobUUID);
        SecHubMessagesList messages = readProductMessages(jobUUID);

        if (messages.getSecHubMessages().isEmpty()) {
            LOG.debug("No messages for job {} found. So skip database access.", jobUUID);
            return;
        }

        LOG.debug("Writing messages to database for job:{}", jobUUID);
        PDSResilientRetryExecutor<IllegalStateException> executor = new PDSResilientRetryExecutor<>(3, pdsJobUpdateExceptionThrower,
                OptimisticLockingFailureException.class);
        executor.execute(() -> {
            jobTransactionService.updateJobMessagesInOwnTransaction(jobUUID, messages);
        }, jobUUID.toString());

    }

    @UseCaseAdminFetchesJobOutputStream(@PDSStep(name = "Update ouptut stream data", description = "Reads output stream data from workspace files and stores content inside database. Will also refresh update time stamp for caching mechanism.", number = 4))
    @UseCaseAdminFetchesJobErrorStream(@PDSStep(name = "Update error stream data", description = "Reads error stream data from workspace files and stores content inside database. Will also refresh update time stamp for caching mechanism.", number = 4))
    @UseCaseAdminFetchesJobMetaData(@PDSStep(name = "Update meta data", description = "Reads meta data from workspace file and stores content inside database if not null. Will also refresh update time stamp for caching mechanism.", number = 4))
    private void writeJobExecutionDataToDatabase(UUID jobUUID) {
        LOG.debug("Writing job execution data to database for job:{}", jobUUID);

        final PDSExecutionData executionData = readJobExecutionData(jobUUID);

        PDSResilientRetryExecutor<IllegalStateException> executor = new PDSResilientRetryExecutor<>(3, pdsJobUpdateExceptionThrower,
                OptimisticLockingFailureException.class);
        executor.execute(() -> {
            jobTransactionService.updateJobExecutionDataInOwnTransaction(jobUUID, executionData);
            return null;
        }, jobUUID.toString());

    }

    private SecHubMessagesList readProductMessages(UUID jobUUID) {
        File productMessagesFolder = workspaceService.getMessagesFolder(jobUUID);

        List<SecHubMessage> collected = messageCollector.collect(productMessagesFolder);

        return new SecHubMessagesList(collected);
    }

    private PDSExecutionData readJobExecutionData(UUID jobUUID) {
        String encoding = workspaceService.getFileEncoding(jobUUID);

        PDSExecutionData executionData = new PDSExecutionData();

        readOutputStream(jobUUID, encoding, executionData);
        readErrorStream(jobUUID, encoding, executionData);
        readMetaData(jobUUID, encoding, executionData);

        return executionData;
    }

    private void readMetaData(UUID jobUUID, String encoding, PDSExecutionData executionData) {
        /* handle meta data file */
        File metaDataFile = workspaceService.getMetaDataFile(jobUUID);
        if (!metaDataFile.exists()) {
            return;
        }
        try {
            executionData.metaData = FileUtils.readFileToString(metaDataFile, encoding);
        } catch (IOException e) {
            LOG.error("Was not able to fetch meta data for PDS job:{} on path:{}!", jobUUID, metaDataFile.getAbsolutePath(), e);
        }

    }

    private void readErrorStream(UUID jobUUID, String encoding, PDSExecutionData executionData) {
        /* handle error stream */
        File systemErrorFile = workspaceService.getSystemErrorFile(jobUUID);
        if (!systemErrorFile.exists()) {
            return;
        }
        try {
            executionData.errorStreamData = FileUtils.readFileToString(systemErrorFile, encoding);
        } catch (IOException e) {
            LOG.error("Was not able to fetch error stream data for PDS job:{} on path:{}!", jobUUID, systemErrorFile.getAbsolutePath(), e);
        }
    }

    private void readOutputStream(UUID jobUUID, String encoding, PDSExecutionData executionData) {
        /* handle output stream */
        File systemOutFile = workspaceService.getSystemOutFile(jobUUID);
        if (!systemOutFile.exists()) {
            return;
        }
        try {
            executionData.outputStreamData = FileUtils.readFileToString(systemOutFile, encoding);
        } catch (IOException e) {
            LOG.error("Was not able to fetch output stream data for PDS job:{} on path:{}!", jobUUID, systemOutFile.getAbsolutePath(), e);
        }
    }

    private String maximum1024chars(String content) {
        if (content == null) {
            return null;
        }
        if (content.length() < 1024) {
            return content;
        }
        return content.substring(0, 1021) + "...";
    }

    private void createProcess(UUID jobUUID, PDSJobConfiguration config, String path) throws IOException {
        if (path == null) {
            throw new IllegalStateException("Path not defined for product id:" + config.getProductId());
        }

        File currentDir = Paths.get("./").toRealPath().toFile();
        List<String> commands = new ArrayList<>();
        commands.add(path);

        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.directory(currentDir);
        builder.inheritIO();

        builder.redirectOutput(workspaceService.getSystemOutFile(jobUUID));
        builder.redirectError(workspaceService.getSystemErrorFile(jobUUID));

        /*
         * add parts from PDS job configuration - means data defined by caller before
         * job was marked as ready to start
         */
        Map<String, String> environment = builder.environment();

        Map<String, String> buildEnvironmentMap = environmentService.buildEnvironmentMap(config);
        environment.putAll(buildEnvironmentMap);

        WorkspaceLocationData locationData = workspaceService.createLocationData(jobUUID);

        environment.put(PDS_JOB_WORKSPACE_LOCATION, locationData.getWorkspaceLocation());
        environment.put(PDS_JOB_RESULT_FILE, locationData.getResultFileLocation());
        environment.put(PDS_JOB_USER_MESSAGES_FOLDER, locationData.getUserMessagesLocation());
        environment.put(PDS_JOB_EVENTS_FOLDER, locationData.getEventsLocation());
        environment.put(PDS_JOB_METADATA_FILE, locationData.getMetaDataFileLocation());
        environment.put(PDS_JOB_UUID, jobUUID.toString());
        environment.put(PDS_JOB_SOURCECODE_ZIP_FILE, locationData.getSourceCodeZipFileLocation());
        environment.put(PDS_JOB_BINARIES_TAR_FILE, locationData.getBinariesTarFileLocation());

        String extractedSourcesLocation = locationData.getExtractedSourcesLocation();

        environment.put(PDS_JOB_SOURCECODE_UNZIPPED_FOLDER, extractedSourcesLocation);
        environment.put(PDS_JOB_EXTRACTED_SOURCES_FOLDER, extractedSourcesLocation);

        String extractedBinariesLocation = locationData.getExtractedBinariesLocation();
        environment.put(PDS_JOB_EXTRACTED_BINARIES_FOLDER, extractedBinariesLocation);

        environment.put(PDS_JOB_HAS_EXTRACTED_SOURCES, "" + workspaceService.hasExtractedSources(jobUUID));
        environment.put(PDS_JOB_HAS_EXTRACTED_BINARIES, "" + workspaceService.hasExtractedBinaries(jobUUID));

        LOG.debug("Prepared launcher script process for job with uuid:{}, path={}, buildEnvironmentMap={}", jobUUID, path, buildEnvironmentMap);

        LOG.info("Start launcher script for job {}", jobUUID);
        try {

            process = processAdapterFactory.startProcess(builder);

        } catch (IOException e) {
            LOG.error("Process start failed for pdsJobUUID:{}. Current directory was:{}", jobUUID, currentDir.getAbsolutePath());
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
        if (process == null || !process.isAlive()) {
            return true;
        }
        cancelOperationsHasBeenStarted = true;

        ProcessHandlingData processHandlingData = null;

        if (config == null) {
            LOG.warn("No configuration available for job:{}. Cannot create dedicated process handling data. No process wait possible.", pdsJobUUID);
        } else {
            processHandlingData = handlingDataFactory.createForCancelOperation(config);
        }

        try {
            handleProcessCancellation(processHandlingData);
            return true;
        } catch (RuntimeException e) {
            return false;
        } finally {

            JobConfigurationData data = jobTransactionService.getJobConfigurationData(pdsJobUUID);

            try {
                PDSJobConfiguration config = PDSJobConfiguration.fromJSON(data.getJobConfigurationJson());
                cleanUpWorkspace(pdsJobUUID, config);
            } catch (PDSJSONConverterException e) {
                LOG.error("Was not able fetch job config for {} - workspace clean only workspace files", pdsJobUUID, e);
            }

        }

    }

    private void handleProcessCancellation(ProcessHandlingData processHandlingData) {

        if (isWaitOnCancelOperationAccepted(processHandlingData)) {
            int millisecondsToWaitForNextCheck = processHandlingData.getMillisecondsToWaitForNextCheck();
            LOG.info("Cancel job: {}: give process chance to cancel. Will wait a maximum time of {} seconds. The check intervall is: {} milliseconds",
                    pdsJobUUID, processHandlingData.getSecondsToWaitForProcess(), millisecondsToWaitForNextCheck);

            while (processHandlingData.isStillWaitingForProcessAccepted()) {
                if (process.isAlive()) {
                    try {

                        LOG.debug("Cancel job: {}: wait {} milliseconds before next process alive check.", pdsJobUUID, millisecondsToWaitForNextCheck);
                        Thread.sleep(millisecondsToWaitForNextCheck);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    LOG.info("Process of job: {} is no longer alive", pdsJobUUID);
                    break;
                }
            }
            LOG.info("Cancel job: {}: waited {} milliseconds at all", pdsJobUUID, System.currentTimeMillis() - processHandlingData.getProcessStartTimeStamp());

        } else {
            LOG.info("Cancel job: {}: will not wait.", pdsJobUUID);
        }
        if (process.isAlive()) {
            LOG.info("Cancel job: {}: still alive, will destroy underlying process forcibly.", pdsJobUUID);
            process.destroyForcibly();
        } else {
            LOG.info("Cancel job: {}: has terminated itself.", pdsJobUUID);
        }
    }

    private boolean isWaitOnCancelOperationAccepted(ProcessHandlingData processHandlingData) {
        if (processHandlingData == null) {
            LOG.debug("Not waiting because no process handling data!");
            return false;
        }
        return processHandlingData.isStillWaitingForProcessAccepted();
    }

    private void cleanUpWorkspace(UUID jobUUID, PDSJobConfiguration config) {
        if (workspaceService.isWorkspaceAutoCleanDisabled()) {
            LOG.info("Auto cleanup is disabled, so keep files at {}", workspaceService.getWorkspaceFolder(jobUUID));
            return;
        }
        try {
            workspaceService.cleanup(jobUUID, config);
            LOG.debug("workspace cleanup done for job:{}", jobUUID);
        } catch (IOException e) {
            LOG.error("workspace cleanup failed for job:{}!", jobUUID);
        }
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

                if (jobStatusService.isJobStreamUpdateNecessary(jobUUID)) {
                    writeJobExecutionDataToDatabase(jobUUID);
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