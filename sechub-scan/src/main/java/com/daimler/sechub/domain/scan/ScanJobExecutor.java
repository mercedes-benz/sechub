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

            LOG.debug("will wait {} milliseconds before cancel checks",millisecondsToWaitBeforeCancelCheck);
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
        handlErrors(canceableJobRunner);
    }

    private void handlErrors(CanceableScanJobRunnable canceableJobRunner) throws SecHubExecutionException {

        SecHubExecutionException exception = canceableJobRunner.exception;
        if (exception == null) {
            /* no failure - so just return */
            return;
        }
        /* abdoned exception are treated special: executor will NOT persist result in this case! */
        if (exception instanceof SecHubExecutionAbandonedException) {
            throw exception; // just rethrow abandoned
        }
        if (progress instanceof Abandonable) {
            Abandonable abandonable = (Abandonable) progress;
            if (abandonable.isAbandoned()) {
                throw new SecHubExecutionAbandonedException("A failure happend, but already abandoned job",exception);
            }
        }
        /* not abandonded, so return failure by exception - will result in stored product result failure*/
        if (exception != null) {
            throw exception;
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
            throw new SecHubExecutionAbandonedException("Abandonded job " + sechubJobUUID + " because canceled",null);
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