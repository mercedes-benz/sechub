// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.mercedesbenz.sechub.domain.scan.product.CodeScanProductExecutionService;
import com.mercedesbenz.sechub.domain.scan.product.InfrastructureScanProductExecutionService;
import com.mercedesbenz.sechub.domain.scan.product.LicenseScanProductExecutionService;
import com.mercedesbenz.sechub.domain.scan.product.WebScanProductExecutionService;
import com.mercedesbenz.sechub.sharedkernel.LogConstants;

/**
 * This class is the primary part for triggering product execution. It is run
 * and inspected by {@link ScanJobExecutor}
 *
 */
class ScanJobExecutionRunnable implements Runnable, CanceableScanJob {

    private static final Logger LOG = LoggerFactory.getLogger(ScanJobExecutionRunnable.class);

    private ScanJobRunnableData runnableData;

    ScanJobExecutionRunnable(ScanJobRunnableData runnableData) {
        this.runnableData = runnableData;
    }

    public ScanJobRunnableData getRunnableData() {
        return runnableData;
    }

    @Override
    public void run() {
        /* runs in own thread so we set job uuid to MDC here ! */
        try {
            MDC.clear();
            MDC.put(LogConstants.MDC_SECHUB_JOB_UUID, runnableData.getSechubJobUUID().toString());
            LOG.info("Beign start of execution services for SecHub job: {}", runnableData.getSechubJobUUID());

            SecHubExecutionContext executionContext = runnableData.getExecutionContext();
            ProductExecutionServiceContainer executionServiceContainer = runnableData.getExecutionServiceContainer();

            CodeScanProductExecutionService codeScanProductExecutionService = executionServiceContainer.getCodeScanProductExecutionService();
            WebScanProductExecutionService webScanProductExecutionService = executionServiceContainer.getWebScanProductExecutionService();
            InfrastructureScanProductExecutionService infraScanProductExecutionService = executionServiceContainer.getInfraScanProductExecutionService();
            LicenseScanProductExecutionService licenseScanProductExecutionService = executionServiceContainer.getLicenseScanProductExecutionService();

            codeScanProductExecutionService.executeProductsAndStoreResults(executionContext);
            webScanProductExecutionService.executeProductsAndStoreResults(executionContext);
            infraScanProductExecutionService.executeProductsAndStoreResults(executionContext);
            licenseScanProductExecutionService.executeProductsAndStoreResults(executionContext);

        } catch (SecHubExecutionException e) {
            runnableData.setException(e);
        } catch (Exception e) {
            LOG.error("Unhandled exception appeared!", e);
        } finally {
            MDC.clear();
        }
    }

    public void cancelScanJob() {
        SecHubExecutionContext executionContext = runnableData.getExecutionContext();
        Thread executorThread = runnableData.getRunnableThread();

        executionContext.markCancelRequested(); // Using this method, the cancel request can be checked in multiple threads

        LOG.info("Will interrupt scan job thread because of cancel operation: {}", executorThread.getName());

        executorThread.interrupt();

    }

}