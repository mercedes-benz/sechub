// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsRecevingSyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessageAnswer;
import com.mercedesbenz.sechub.sharedkernel.messaging.JobFinding;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;

@Component
public class SecHubExplanationInputCollector {

    private final DomainMessageService domainMessageService;

    SecHubExplanationInputCollector(DomainMessageService domainMessageService) {
        this.domainMessageService = domainMessageService;
    }

    @IsSendingSyncMessage(MessageID.REQUEST_DETAILS_FOR_JOB_FINDING)
    @IsRecevingSyncMessage(MessageID.RESULT_DETAILS_FOR_JOB_FINDING)
    @IsSendingSyncMessageAnswer(value = MessageID.REQUEST_DETAILS_FOR_JOB_FINDING, answeringTo = MessageID.RESULT_DETAILS_FOR_JOB_FINDING, branchName = "success")
    public SecHubExplanationInput collectInput(String projectId, UUID jobUUID, int findingId) {

        DomainMessage domainMessage = new DomainMessage(MessageID.REQUEST_DETAILS_FOR_JOB_FINDING);
        JobFinding findingData = new JobFinding();
        findingData.setJobUUID(jobUUID);
        findingData.setFindingId(findingId);
        findingData.setProjectId(projectId);

        domainMessage.set(MessageDataKeys.JOB_FINDING_DATA, findingData);

        DomainMessageSynchronousResult result = domainMessageService.sendSynchron(domainMessage);

        JobFinding data = result.get(MessageDataKeys.JOB_FINDING_DATA);

        SecHubExplanationInput input = new SecHubExplanationInput();
        input.setAvailable(data.isAvailable());

        if (data.isAvailable()) {
            /* we only set data when accessible */
            input.setCweId(data.getCweId());
            input.setRelevantSource(data.getRelevantSource());
            input.setFileName(data.getFileName());
            input.setFindingName(data.getFindingName());
            input.setFindingDescription(data.getFindingDescription());
        }
        return input;
    }

}
