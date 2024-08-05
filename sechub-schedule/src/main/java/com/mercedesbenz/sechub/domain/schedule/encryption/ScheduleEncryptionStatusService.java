// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubDomainEncryptionData;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubDomainEncryptionStatus;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsRecevingSyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessageAnswer;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.SynchronMessageHandler;

@Service
public class ScheduleEncryptionStatusService implements SynchronMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleEncryptionStatusService.class);

    @Autowired
    ScheduleCipherPoolDataRepository poolDataRepository;

    @Autowired
    SecHubJobRepository jobRepository;

    @Override
    public DomainMessageSynchronousResult receiveSynchronMessage(DomainMessage request) {
        LOG.debug("received synchronnous domain request: {}", request);

        MessageID messageId = request.getMessageId();

        switch (messageId) {
        case GET_ENCRYPTION_STATUS_SCHEDULE_DOMAIN:
            return handleEncryptionStatusRequest();
        default:
            throw new IllegalStateException("unhandled message id:" + messageId);
        }

    }

    @IsRecevingSyncMessage(MessageID.GET_ENCRYPTION_STATUS_SCHEDULE_DOMAIN)
    @IsSendingSyncMessageAnswer(value = MessageID.RESULT_ENCRYPTION_STATUS_SCHEDULE_DOMAIN, answeringTo = MessageID.GET_ENCRYPTION_STATUS_SCHEDULE_DOMAIN, branchName = "success")
    private DomainMessageSynchronousResult handleEncryptionStatusRequest() {

        SecHubDomainEncryptionStatus status = createEncryptionStatus();

        DomainMessageSynchronousResult result = new DomainMessageSynchronousResult(MessageID.RESULT_ENCRYPTION_STATUS_SCHEDULE_DOMAIN);
        result.set(MessageDataKeys.SECHUB_DOMAIN_ENCRYPTION_STATUS, status);
        return result;
    }

    public SecHubDomainEncryptionStatus createEncryptionStatus() {
        SecHubDomainEncryptionStatus status = new SecHubDomainEncryptionStatus();
        status.setName("schedule");

        List<ScheduleCipherPoolData> all = poolDataRepository.findAll();

        for (ScheduleCipherPoolData cipherPoolData : all) {
            Long cipherPoolid = cipherPoolData.getId();

            // initialize
            SecHubDomainEncryptionData data = new SecHubDomainEncryptionData();
            data.setId(String.valueOf(cipherPoolid));
            data.setAlgorithm(cipherPoolData.getAlgorithm());
            data.getPasswordSource().setType(cipherPoolData.getPasswordSourceType());
            data.getPasswordSource().setData(cipherPoolData.getPasswordSourceData());
            data.setCreated(cipherPoolData.getCreated());
            data.setCreatedFrom(cipherPoolData.getCreatedFrom());

            // add usage
            for (ExecutionState state : ExecutionState.values()) {
                long count = jobRepository.countJobsInExecutionStateAndEncryptedWithPoolId(state, cipherPoolid);
                data.getUsage().put("job.state." + state.toString().toLowerCase(), count);
            }
            status.getData().add(data);

        }

        return status;
    }

}
