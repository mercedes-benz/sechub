// SPDX-License-Identifier: MIT
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
        Thread canceableJobThread = new Thread(canceableJobRunner, "SecHub-exec-" + context.getTraceLogId().getPlainId() + "-" + progress.getId());
        canceableJobRunner.executorThread = canceableJobThread;

        UUID sechubJobUUID = context.getSechubJobUUID();
        try {
            canceableJobThread.start();

            this.scanService.scanJobListener.started(sechubJobUUID, canceableJobRunner);

            /* wait for job runnable - except when canceled */
            while (canceableJobThread.isAlive()) {
                try {
                    LOG.debug("will wait max {} milliseconds before cancel checks - job thread is:{}", millisecondsToWaitBeforeCancelCheck, canceableJobThread.getName());
                    /* we simply join scan thread until we do next cancel check */
                    canceableJobThread.join(millisecondsToWaitBeforeCancelCheck);

                    if (progress.isCanceled()) {
                        handleCanceled(canceableJobRunner, sechubJobUUID);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            SecHubExecutionException exception = canceableJobRunner.exception;
            handleErrors(exception);
            
        }catch(Exception e) {
            /* should never happen, because all handled by runnable, but...*/
            handleErrors(new SecHubExecutionException("Scan failed - but not handled by runnable.", e));
        } finally {
            this.scanService.scanJobListener.ended(sechubJobUUID);

        }
    }

    private void handleErrors(SecHubExecutionException exception) throws SecHubExecutionException {

        
        if (exception == null) {
            /* no failure - so just return */
            return;
        }
        /*
         * abdoned exception are treated special: executor will NOT persist result in
         * this case!
         */
        if (exception instanceof SecHubExecutionAbandonedException) {
            LOG.debug("Rethrow SecHubExecutionAbandonedException");
            throw exception; // just rethrow abandoned
        }
        LOG.debug("No SecHubExecutionAbandonedException");
        if (progress instanceof Abandonable) {
            LOG.debug("Start abandoble check");
            Abandonable abandonable = (Abandonable) progress;
            if (abandonable.isAbandoned()) {
                LOG.debug("Done abandoble check- IS abandonded");
                throw new SecHubExecutionAbandonedException(context,"A failure happend, but already abandoned job", exception);
            }
            LOG.debug("Done abandoble check- not abandonded");

        }
        LOG.debug("Rethrow normal sechub execution exception");
        throw exception;
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
            throw new SecHubExecutionAbandonedException(context,"Abandonded job " + sechubJobUUID + " because canceled", null);
        }
    }

    /**
     * This class is the primary part for triggering product exection
     * @author Albert Tregnaghi
     *
     */
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
            context.markCanceled(); // we mark the context as canceled, so can be checked in multiple threads
            executorThread.interrupt();
            LOG.info("Marked scan job thread canceled :{}", executorThread.getName());
        }

    }

}