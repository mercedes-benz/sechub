// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static java.util.Objects.*;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.sharedkernel.NullProgressStateFetcher;
import com.mercedesbenz.sechub.sharedkernel.ProgressState;
import com.mercedesbenz.sechub.sharedkernel.ProgressStateFetcher;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.other.UseCaseSystemSuspendsJobsWhenSigTermReceived;

/**
 * Finally executes the scan job
 */
class ScanJobExecutor {
    private static final int DEFAUL_PROGRESS_CHECK_TIME_IN_MILLISECONDS = 100;
    private static final String SECHUB_SCAN_THREAD_PREFIX = "sechub-scan:";
    private static final String SECHUB_SCAN_CANCEL_THREAD_PREFIX = "sechub-scan-cancel:";

    /* the absolute minimum of time to wait for next cancel check */
    private static final int MINIMUM_PROGRESS_STATE_CHECK_TIME_MILLISECONDS = DEFAUL_PROGRESS_CHECK_TIME_IN_MILLISECONDS;

    private static final Logger LOG = LoggerFactory.getLogger(ScanJobExecutor.class);

    private final ProductExecutionServiceContainer executionServiceContainer;
    private SecHubExecutionContext context;

    private ProgressStateFetcher progressFetcher;

    private int millisecondsToWaitBeforeCancelAndPauseCheck;

    private ScanJobListener scanJobListener;

    ScanJobExecutor(ProductExecutionServiceContainer serviceContainer, ScanJobListener scanJobListener, SecHubExecutionContext context,
            ProgressStateFetcher progressFetcher, int millisecondsToWaitBeforeProgressStateCheck) {
        this.executionServiceContainer = serviceContainer;
        this.scanJobListener = scanJobListener;
        this.context = context;
        if (progressFetcher == null) {
            progressFetcher = new NullProgressStateFetcher();
        }
        this.progressFetcher = progressFetcher;
        if (millisecondsToWaitBeforeProgressStateCheck < MINIMUM_PROGRESS_STATE_CHECK_TIME_MILLISECONDS) {
            millisecondsToWaitBeforeProgressStateCheck = DEFAUL_PROGRESS_CHECK_TIME_IN_MILLISECONDS;
        }
        this.millisecondsToWaitBeforeCancelAndPauseCheck = millisecondsToWaitBeforeProgressStateCheck;
    }

    /**
     * Starts the scan operations for the job inside this context. If a cancel
     * request is recognized, the scan will be interrupted as fast as possible
     *
     * @throws SecHubExecutionException
     */
    @UseCaseSystemSuspendsJobsWhenSigTermReceived(@Step(number = 4, name = "Scan job executor stops suspended jobs", description = "Scheduler instance has marked jobs as suspended. Will stop execution of scans of these jobs"))
    void startScanAndInspectCancelRequests() throws SecHubExecutionException {
        SecHubExecutionOperationType operationType = context.getOperationType();
        if (!SecHubExecutionOperationType.SCAN.equals(operationType)) {
            throw new IllegalStateException("The operationt type must be " + SecHubExecutionOperationType.SCAN + " but was:" + operationType);
        }

        UUID sechubJobUUID = context.getSechubJobUUID();
        requireNonNull(sechubJobUUID, "sechubJobUUID must be defined!");

        ScanJobRunnableData runnableData = new ScanJobRunnableData(sechubJobUUID, executionServiceContainer, context);

        ScanJobExecutionRunnable scanJobExecutionRunnable = new ScanJobExecutionRunnable(runnableData);
        // In next line we add "-scan" to end of thread name, to have info in logging
        // when thread name is reduced
        String threadName = SECHUB_SCAN_THREAD_PREFIX + sechubJobUUID + "-scan";
        Thread executorThread = new Thread(scanJobExecutionRunnable, threadName);
        runnableData.setScanJobExecutionThread(executorThread);

        try {
            /* start scan thread */
            executorThread.start();

            scanJobListener.started(sechubJobUUID, scanJobExecutionRunnable);

            /* wait for job runnable - except when canceled */
            while (executorThread.isAlive()) {
                try {
                    LOG.debug("will wait max {} milliseconds before cancel and suspend checks - job thread is:{}", millisecondsToWaitBeforeCancelAndPauseCheck,
                            executorThread.getName());

                    /* we simply join scan thread until we do next cancel check */
                    executorThread.join(millisecondsToWaitBeforeCancelAndPauseCheck);

                    ProgressState state = progressFetcher.fetchProgressState();

                    LOG.debug("progress state: canceled = {}, suspended = {}", state.isCanceled(), state.isSuspended());

                    if (state.isCanceled()) {

                        handleCancelRequested(scanJobExecutionRunnable, sechubJobUUID);

                    } else if (state.isSuspended()) {

                        handleSuspendRequest(scanJobExecutionRunnable, sechubJobUUID);
                        // here we do not handle errors etc. - only finally block willl be done.

                        return;
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            SecHubExecutionException exception = runnableData.getException();
            handleErrors(exception);

        } catch (Exception e) {
            if (runnableData.getExecutionContext().isSuspended()) {
                /* we ignore here the error - this job is just suspending */
                scanJobListener.suspended(sechubJobUUID);
            } else {
                handleErrors(new SecHubExecutionException("Scan execution failed - but not handled by runnable.", e));
            }

        } finally {
            scanJobListener.ended(sechubJobUUID);

        }
    }

    private void handleSuspendRequest(ScanJobExecutionRunnable scanJobExecutionRunnable, UUID sechubJobUUID) {
        /* means we must stop processing here as soon as possible */
        LOG.warn("Suspend requested - will interrupt SecHub job: {}", sechubJobUUID);
        scanJobExecutionRunnable.getRunnableData().getExecutionContext().markSuspended();

        scanJobExecutionRunnable.suspend();

        /*
         * we do not send any cancel events here - reason: PDS instances shall still do
         * their work, the suspend is only on SecHub side.
         */

    }

    private void handleErrors(SecHubExecutionException exception) throws SecHubExecutionException {
        if (exception == null) {
            /* no failure - so just return */
            return;
        }
        throw exception;
    }

    private void handleCancelRequested(ScanJobExecutionRunnable executionRunable, UUID sechubJobUUID) {
        LOG.info("Received cancel signal, so start canceling job: {}", sechubJobUUID);

        ScanJobRunnableData data = executionRunable.getRunnableData();
        if (data.getException() != null) {
            /*
             * there was a failure at execution time - so do not cancel but instad return
             * only, so error handling can be done
             */
            return;
        }
        startCancelThreadIfNecessary(data);

    }

    private void startCancelThreadIfNecessary(ScanJobRunnableData data) {
        SecHubExecutionContext executionContext = data.getExecutionContext();

        SecHubExecutionHistory history = executionContext.getExecutionHistory();
        if (history.isEmpty()) {
            LOG.info("No history elements found, so will not trigger any cancel operation by product executors");
            return;

        } else if (history.getAllElementsWithCanceableProductExecutors().isEmpty()) {
            LOG.info("History elements found, but none was canceable so will not trigger any cancel operation by product executors");
            return;

        }
        /*
         * History elements found - means at least one product executor was still doing
         * its job while being interrupted. So cancel thread necessary.
         */
        ScanJobCancellationRunnable cancelRunnable = new ScanJobCancellationRunnable(data);
        // In next line we add "-cancel" to end of thread name, to have info in logging
        // when thread name is reduced
        String threadName = SECHUB_SCAN_CANCEL_THREAD_PREFIX + data.getSechubJobUUID() + "-cancel";
        Thread cancelThread = new Thread(cancelRunnable, threadName);
        data.setScanJobCancelThread(cancelThread);
        cancelThread.start();
    }

}