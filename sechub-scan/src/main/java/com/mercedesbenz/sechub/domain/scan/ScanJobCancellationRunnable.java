// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.mercedesbenz.sechub.domain.scan.product.CanceableProductExecutor;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutor;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorData;
import com.mercedesbenz.sechub.sharedkernel.LogConstants;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.JobMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;

/**
 * This class is the primary part for cancelling product execution. It is run
 * and by {@link ScanJobExecutor}
 *
 */
class ScanJobCancellationRunnable implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ScanJobCancellationRunnable.class);

    private ScanJobRunnableData runnableData;

    ScanJobCancellationRunnable(ScanJobRunnableData runnableData) {
        this.runnableData = runnableData;
    }

    @Override
    public void run() {
        /* runs in own thread so we set job uuid to MDC here ! */
        try {
            MDC.clear();
            MDC.put(LogConstants.MDC_SECHUB_JOB_UUID, runnableData.getSechubJobUUID().toString());
            MDC.put(LogConstants.MDC_SECHUB_EXECUTION_UUID, runnableData.getSechubExecutioUUID().toString());

            LOG.info("Begin cancellation of hard interrupted product executors for SecHub job: {}", runnableData.getSechubJobUUID());

            SecHubExecutionContext executionContext = runnableData.getExecutionContext();
            List<SecHubExecutionHistoryElement> historyElementsToCancel = executionContext.getExecutionHistory().getAllElementsWithCanceableProductExecutors();

            for (SecHubExecutionHistoryElement historyElementToCancel : historyElementsToCancel) {
                ProductExecutor executor = historyElementToCancel.getProductExecutor();
                ProductExecutorData data = historyElementToCancel.getProductExecutorData();
                if (executor == null) {
                    throw new IllegalStateException("Executor may not be null in history!");
                }
                if (data == null) {
                    throw new IllegalStateException("Data may not be null in history!");
                }
                if (executor instanceof CanceableProductExecutor) {

                    LOG.info("Try to cancel product execution via executor: {} for job: {}", executor.getIdentifier(), runnableData.getSechubJobUUID());
                    CanceableProductExecutor canceableProductExecutor = (CanceableProductExecutor) executor;
                    boolean canceled = canceableProductExecutor.cancel(data);

                    if (canceled) {
                        LOG.info("Success:Cancellation was triggered to product by executor:{}", canceableProductExecutor.getIdentifier());
                    } else {
                        LOG.warn("Failed:Cancellation was triggered to product but was not successful! Used executor:{}",
                                canceableProductExecutor.getIdentifier());
                    }

                } else {
                    LOG.error("The product executor: {} is not canceable, but was inside list.", executor.getIdentifier());
                }

            }

            sendEventThatProduductExecutorsHaveBeenCanceled();

        } catch (Exception e) {
            LOG.error("Unhandled exception appeared!", e);
        } finally {

            MDC.clear();
        }
    }

    @IsSendingAsyncMessage(MessageID.PRODUCT_EXECUTOR_CANCEL_OPERATIONS_DONE)
    private void sendEventThatProduductExecutorsHaveBeenCanceled() {
        DomainMessageService domainMessageService = runnableData.getExecutionServiceContainer().getDomainMessageService();
        DomainMessage request = new DomainMessage(MessageID.PRODUCT_EXECUTOR_CANCEL_OPERATIONS_DONE);

        JobMessage jobCancelData = new JobMessage();
        jobCancelData.setJobUUID(runnableData.getSechubJobUUID());
        request.set(MessageDataKeys.JOB_CANCEL_DATA, jobCancelData);
        request.set(MessageDataKeys.SECHUB_EXECUTION_UUID, runnableData.getSechubExecutioUUID());
        domainMessageService.sendAsynchron(request);
    }

}