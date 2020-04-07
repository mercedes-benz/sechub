package com.daimler.sechub.domain.scan;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;

class ScanJobExecutor {
    

    private static final Logger LOG = LoggerFactory.getLogger(ScanJobExecutor.class);

    private final ScanService scanService;
    private SecHubExecutionContext context;

    ScanJobExecutor(ScanService scanService, SecHubExecutionContext context) {
        this.scanService = scanService;
        this.context = context;
    }
    
    public void execute() throws SecHubExecutionException {
        CanceableScanJobRunnable canceableJobRunner = new CanceableScanJobRunnable();
        Thread t = new Thread(canceableJobRunner, "executor-job-" + context.getTraceLogId().getPlainId());
        canceableJobRunner.executorThread=t;
        
        UUID sechubJobUUID = context.getSechubJobUUID();
        try {
            t.start();
            
            this.scanService.scanJobService.register(sechubJobUUID, canceableJobRunner);
            
            t.join(); // wait for execution done
        } catch (InterruptedException e) {
            throw new SecHubExecutionException("Job " + sechubJobUUID + " was interrupted", e);
        } finally {
            this.scanService.scanJobService.unregister(sechubJobUUID);
        }
        if (canceableJobRunner.exception != null) {
            throw canceableJobRunner.exception;
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
            if (executorThread==null) {
                LOG.error("No executor thread set, cannot cancel!");
                return;
            }
            LOG.info("Interupting thread:{}",executorThread.getName());
            executorThread.interrupt();
        }

    }

   

}