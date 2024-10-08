// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.util.UUID;

import com.mercedesbenz.sechub.sharedkernel.MutableProgressState;
import com.mercedesbenz.sechub.sharedkernel.ProgressState;
import com.mercedesbenz.sechub.sharedkernel.ProgressStateFetcher;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.SchedulerJobMessage;

public class ScanProgressStateFetcher implements ProgressStateFetcher {

    private DomainMessageService eventBus;
    private UUID sechubJobUUID;

    ScanProgressStateFetcher(DomainMessageService eventBus, UUID sechubJobUUID) {
        this.eventBus = eventBus;
        this.sechubJobUUID = sechubJobUUID;
    }

    @Override
    public ProgressState fetchProgressState() {
        SchedulerJobMessage jobStatusResponse = sendRequestBatchJobStatusRequestSynchron();

        MutableProgressState state = new MutableProgressState();
        state.setCanceled(jobStatusResponse.isCancelRequested() || jobStatusResponse.isCanceled());
        state.setSuspended(jobStatusResponse.isSuspended());

        return state;
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

}
