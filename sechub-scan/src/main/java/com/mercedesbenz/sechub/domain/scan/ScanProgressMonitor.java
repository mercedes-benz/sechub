// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.util.UUID;

import com.mercedesbenz.sechub.sharedkernel.ProgressMonitor;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.SchedulerJobMessage;

public class ScanProgressMonitor implements ProgressMonitor {

    private DomainMessageService eventBus;
    private UUID sechubJobUUID;

    ScanProgressMonitor(DomainMessageService eventBus, UUID sechubJobUUID) {
        this.eventBus = eventBus;
        this.sechubJobUUID = sechubJobUUID;
    }

    @Override
    public boolean isCanceled() {
        SchedulerJobMessage jobStatusResponse = sendRequestBatchJobStatusRequestSynchron();
        /*
         * we accept both states here - for the progress it does not matter: it is
         * canceled
         */
        return jobStatusResponse.isCancelRequested() || jobStatusResponse.isCanceled();
    }

    @IsSendingSyncMessage(MessageID.REQUEST_SCHEDULER_JOB_STATUS)
    protected SchedulerJobMessage sendRequestBatchJobStatusRequestSynchron() {
        DomainMessage request = new DomainMessage(MessageID.REQUEST_SCHEDULER_JOB_STATUS);
        SchedulerJobMessage statusRequestMessage = new SchedulerJobMessage();
        statusRequestMessage.setSecHubJobUUID(sechubJobUUID);
        request.set(MessageDataKeys.SCHEDULER_JOB_STATUS, statusRequestMessage);

        /* ask for status */
        DomainMessageSynchronousResult response = eventBus.sendSynchron(request);
        SchedulerJobMessage jobStatusRepsonse = response.get(MessageDataKeys.SCHEDULER_JOB_STATUS);
        return jobStatusRepsonse;
    }

    @Override
    public String getId() {
        return "" + sechubJobUUID;
    }

}
