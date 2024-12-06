// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.mercedesbenz.sechub.domain.scan.product.AnalyticsProductExecutionService;
import com.mercedesbenz.sechub.domain.scan.product.CodeScanProductExecutionService;
import com.mercedesbenz.sechub.domain.scan.product.InfrastructureScanProductExecutionService;
import com.mercedesbenz.sechub.domain.scan.product.LicenseScanProductExecutionService;
import com.mercedesbenz.sechub.domain.scan.product.PrepareProductExecutionService;
import com.mercedesbenz.sechub.domain.scan.product.SecretScanProductExecutionService;
import com.mercedesbenz.sechub.domain.scan.product.WebScanProductExecutionService;
import com.mercedesbenz.sechub.sharedkernel.LogConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseSchedulerStartsJob;
import com.mercedesbenz.sechub.sharedkernel.usecases.other.UseCaseSystemSuspendsJobsWhenSigTermReceived;

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
    @UseCaseSchedulerStartsJob(@Step(number = 3, name = "Runnable calls execution services",

            description = """
                    The job execution runnable creates the execution context and calls dedicated execution services
                    (preparation, analytics, product execution, storage and reporting) synchronously for the job.
                    It is also responsible for cancelation and supsension of jobs.
                    """))
    public void run() {
        /* runs in own thread so we set job uuid to MDC here ! */
        try {
            MDC.clear();
            MDC.put(LogConstants.MDC_SECHUB_JOB_UUID, runnableData.getSechubJobUUID().toString());
            MDC.put(LogConstants.MDC_SECHUB_EXECUTION_UUID, runnableData.getSechubExecutioUUID().toString());

            LOG.info("Starting execution services for SecHub job: {}", runnableData.getSechubJobUUID());

            SecHubExecutionContext executionContext = runnableData.getExecutionContext();
            ProductExecutionServiceContainer executionServiceContainer = runnableData.getExecutionServiceContainer();

            /* prepare phase (e.g. for remote data ) */
            PrepareProductExecutionService prepareProductExecutionService = executionServiceContainer.getPrepareProductExecutionService();
            prepareProductExecutionService.executeProductsAndStoreResults(executionContext);

            if (executionContext.hasPrepareFailed()) {
                LOG.error("Preparation phase failed");
                return;
            }

            /* analytics scan phase */
            AnalyticsProductExecutionService analyticsProductExecutionService = executionServiceContainer.getAnalyticsProductExecutionService();
            analyticsProductExecutionService.executeProductsAndStoreResults(executionContext);

            /* normal scan phase */
            CodeScanProductExecutionService codeScanProductExecutionService = executionServiceContainer.getCodeScanProductExecutionService();
            WebScanProductExecutionService webScanProductExecutionService = executionServiceContainer.getWebScanProductExecutionService();
            InfrastructureScanProductExecutionService infraScanProductExecutionService = executionServiceContainer.getInfraScanProductExecutionService();
            LicenseScanProductExecutionService licenseScanProductExecutionService = executionServiceContainer.getLicenseScanProductExecutionService();
            SecretScanProductExecutionService secretScanProductExecutionService = executionServiceContainer.getSecretScanProductExecutionService();

            codeScanProductExecutionService.executeProductsAndStoreResults(executionContext);
            webScanProductExecutionService.executeProductsAndStoreResults(executionContext);
            infraScanProductExecutionService.executeProductsAndStoreResults(executionContext);
            licenseScanProductExecutionService.executeProductsAndStoreResults(executionContext);
            secretScanProductExecutionService.executeProductsAndStoreResults(executionContext);

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
        Thread executorThread = runnableData.getScanJobExecutionThread();

        executionContext.markCancelRequested(); // Using this method, the cancel request can be checked in multiple threads

        LOG.info("Will interrupt scan job thread because of cancel operation: {}", executorThread.getName());

        executorThread.interrupt();

    }

    @UseCaseSystemSuspendsJobsWhenSigTermReceived(@Step(number = 5, name = "Scan job executable handles SUSPEND state", description = "Scan job executable stops execution because suspended"))
    public void suspend() {
        Thread executorThread = runnableData.getScanJobExecutionThread();

        Thread scanJobCancelThread = runnableData.getScanJobCancelThread();
        if (scanJobCancelThread != null) {
            try {
                LOG.info("Will interrupt scan job cancel thread because of suspend operation: {}", executorThread.getName());
                scanJobCancelThread.interrupt();
            } catch (Exception e) {
                LOG.error("Was not able to interrupt cancel thread: {}", scanJobCancelThread.getName(), e);
            }
        }
        LOG.info("Will interrupt scan job execution thread because of suspend operation. Name of thread = {}", executorThread.getName());
        executorThread.interrupt();

    }

}