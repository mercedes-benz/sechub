// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import com.daimler.sechub.sharedkernel.Abandonable;
import com.daimler.sechub.sharedkernel.ProgressMonitor;
import com.daimler.sechub.sharedkernel.messaging.BatchJobMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.daimler.sechub.sharedkernel.messaging.IsSendingSyncMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;

public class ScanProgressMonitor implements ProgressMonitor, Abandonable {
    
    private DomainMessageService eventBus;
    private long batchJobId;

    ScanProgressMonitor(DomainMessageService eventBus, long batchJobId) {
        this.eventBus = eventBus;
        this.batchJobId=batchJobId;
    }

    @Override
    public boolean isCanceled() {
        BatchJobMessage jobStatusRepsonse = sendRequestBatchJobStatusRequestSynchron();
        return jobStatusRepsonse.isCanceled();
    }
    
    @Override
    public boolean isAbandoned() {
        BatchJobMessage jobStatusRepsonse = sendRequestBatchJobStatusRequestSynchron();
        return jobStatusRepsonse.isAbandoned();
    }

    @IsSendingSyncMessage(MessageID.REQUEST_BATCH_JOB_STATUS)
    protected BatchJobMessage sendRequestBatchJobStatusRequestSynchron() {
        DomainMessage request = new DomainMessage(MessageID.REQUEST_BATCH_JOB_STATUS);
        BatchJobMessage statusRequestMessage = new BatchJobMessage();
        statusRequestMessage.setBatchJobId(batchJobId);
        request.set(MessageDataKeys.BATCH_JOB_STATUS, statusRequestMessage);
        
        /* ask for status */
        DomainMessageSynchronousResult response = eventBus.sendSynchron(request);
        BatchJobMessage jobStatusRepsonse = response.get(MessageDataKeys.BATCH_JOB_STATUS);
        return jobStatusRepsonse;
    }
    
    @Override
    public String getId() {
        return ""+batchJobId;
    }


}
