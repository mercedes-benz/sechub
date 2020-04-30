package com.daimler.sechub.domain.scan;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.sharedkernel.Abandonable;
import com.daimler.sechub.sharedkernel.NullProgressMonitor;
import com.daimler.sechub.sharedkernel.ProgressMonitor;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionAbandonedException;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;

class ScanJobExecutor {
    /* the absolute minimum of time to wait for next cancel check */
    private static final int MINIMUM_CANCEL_CHECK_TIME_MILLISECONDS = 100;

    private static final Logger LOG = LoggerFactory.getLogger(ScanJobExecutor.class);

    private final ScanService scanService;
    private SecHubExecutionContext context;

    private ProgressMonitor progress;

    private int millisecondsToWaitBeforeCancelCheck;

    ScanJobExecutor(ScanService scanService, SecHubExecutionContext context, ProgressMonitor progress, int millisecondsToWaitBeforeCancelCheck) {
        this.scanService = scanService;
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

    public void execute() throws SecHubExecutionException {
        CanceableScanJobRunnable canceableJobRunner = new CanceableScanJobRunnable();
        Thread t = new Thread(canceableJobRunner, "SecHub-exec-" + context.getTraceLogId().getPlainId()+"-"+progress.getId());
        canceableJobRunner.executorThread = t;

        UUID sechubJobUUID = context.getSechubJobUUID();
        try {
            t.start();

            this.scanService.scanJobListener.started(sechubJobUUID, canceableJobRunner);

            /* wait for job runnable - except when canceled */
            while (t.isAlive()) {
                try {
                    /* we simply join scan thread until we do next cancel check */
                    t.join(millisecondsToWaitBeforeCancelCheck);

                    if (progress.isCanceled()) {
                        handleCanceled(canceableJobRunner, sechubJobUUID);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

        } finally {
            this.scanService.scanJobListener.ended(sechubJobUUID);
        }
        if (canceableJobRunner.exception != null) {
            throw canceableJobRunner.exception;
        }
    }

    private void handleCanceled(CanceableScanJobRunnable canceableJobRunner, UUID sechubJobUUID) throws SecHubExecutionAbandonedException {
        LOG.info("Received cancel signal, so start canceling job: {}", sechubJobUUID);
        canceableJobRunner.cancelScanJob();
        if (!(progress instanceof Abandonable)) {
            return;
        }
        Abandonable abandoble = (Abandonable) progress;
        LOG.info("Check if job {} shall be abandoned", sechubJobUUID);
        if (abandoble.isAbandoned()) {
            LOG.info("Must abandon {}", sechubJobUUID);
            throw new SecHubExecutionAbandonedException("Abandonded job " + sechubJobUUID + " because canceled");
        }
    }

    private class CanceableScanJobRunnable implements Runnable, CanceableScanJob {

        private SecHubExecutionException exception;
        public Thread executorThread;

        @Override
        public void run() {
            try {

                scanService.codeScanProductExecutionService.executeProductsAndStoreResults(context);
                scanService.webScanProductExecutionService.executeProductsAndStoreResults(context);
                scanService.infraScanProductExecutionService.executeProductsAndStoreResults(context);

            } catch (SecHubExecutionException e) {
                this.exception = e;
            }
        }

        public void cancelScanJob() {
            if (executorThread == null) {
                LOG.error("No executor thread set, cannot cancel!");
                return;
            }
            LOG.info("Interupting thread:{}", executorThread.getName());
            executorThread.interrupt();
        }

    }

}