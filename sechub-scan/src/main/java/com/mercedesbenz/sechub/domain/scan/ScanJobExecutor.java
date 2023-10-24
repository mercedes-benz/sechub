// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static java.util.Objects.*;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.sharedkernel.NullProgressMonitor;
import com.mercedesbenz.sechub.sharedkernel.ProgressMonitor;

/**
 * Finally executes the scan job
 */
class ScanJobExecutor {
    private static final String SECHUB_SCAN_THREAD_PREFIX = "sechub-scan:";
    private static final String SECHUB_SCAN_CANCEL_THREAD_PREFIX = "sechub-scan-cancel:";

    /* the absolute minimum of time to wait for next cancel check */
    private static final int MINIMUM_CANCEL_CHECK_TIME_MILLISECONDS = 100;

    private static final Logger LOG = LoggerFactory.getLogger(ScanJobExecutor.class);

    private final ProductExecutionServiceContainer executionServiceContainer;
    private SecHubExecutionContext context;

    private ProgressMonitor progress;

    private int millisecondsToWaitBeforeCancelCheck;

    private ScanJobListener scanJobListener;

    ScanJobExecutor(ProductExecutionServiceContainer serviceContainer, ScanJobListener scanJobListener, SecHubExecutionContext context,
            ProgressMonitor progress, int millisecondsToWaitBeforeCancelCheck) {
        this.executionServiceContainer = serviceContainer;
        this.scanJobListener = scanJobListener;
        this.context = context;
        if (progress == null) {
            progress = new NullProgressMonitor();
        }
        this.progress = progress;
        if (millisecondsToWaitBeforeCancelCheck < MINIMUM_CANCEL_CHECK_TIME_MILLISECONDS) {
            millisecondsToWaitBeforeCancelCheck = 100;
        }
        this.millisecondsToWaitBeforeCancelCheck = millisecondsToWaitBeforeCancelCheck;
    }

    /**
     * Starts the scan operations for the job inside this context. If a cancel
     * request is recognized, the scan will be interrupted as fast as possible
     *
     * @throws SecHubExecutionException
     */
    void startScanAndInspectCancelRequests() throws SecHubExecutionException {
        SecHubExecutionOperationType operationType = context.getOperationType();
        if (!SecHubExecutionOperationType.SCAN.equals(operationType)) {
            throw new IllegalStateException("The operationt type must be " + SecHubExecutionOperationType.SCAN + " but was:" + operationType);
        }

        UUID sechubJobUUID = context.getSechubJobUUID();
        requireNonNull(sechubJobUUID, "sechubJobUUID must be defined!");

        ScanJobRunnableData runnableData = new ScanJobRunnableData(sechubJobUUID, executionServiceContainer, context);

        ScanJobExecutionRunnable scanJobExecutionRunnable = new ScanJobExecutionRunnable(runnableData);
        Thread executorThread = new Thread(scanJobExecutionRunnable, SECHUB_SCAN_THREAD_PREFIX + sechubJobUUID);
        runnableData.setRunnableThread(executorThread);

        try {
            executorThread.start();

            scanJobListener.started(sechubJobUUID, scanJobExecutionRunnable);

            /* wait for job runnable - except when canceled */
            while (executorThread.isAlive()) {
                try {
                    LOG.debug("will wait max {} milliseconds before cancel checks - job thread is:{}", millisecondsToWaitBeforeCancelCheck,
                            executorThread.getName());

                    /* we simply join scan thread until we do next cancel check */
                    executorThread.join(millisecondsToWaitBeforeCancelCheck);

                    if (progress.isCanceled()) {
                        handleCancelRequested(scanJobExecutionRunnable, sechubJobUUID);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            SecHubExecutionException exception = runnableData.getException();
            handleErrors(exception);

        } catch (Exception e) {
            /* should never happen, because all handled by runnable, but... */
            handleErrors(new SecHubExecutionException("Scan execution failed - but not handled by runnable.", e));
        } finally {
            scanJobListener.ended(sechubJobUUID);

        }
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
         * history elements found - means at least one product executor was still doing
         * its job while being interrupted. So cancel thread necessary.
         */
        ScanJobCancellationRunnable cancelRunnable = new ScanJobCancellationRunnable(data);
        Thread cancelThread = new Thread(cancelRunnable, SECHUB_SCAN_CANCEL_THREAD_PREFIX + data.getSechubJobUUID());
        data.setRunnableThread(cancelThread);
        cancelThread.start();
    }

}