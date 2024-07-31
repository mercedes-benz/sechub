// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.encryption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubDomainEncryptionStatus;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionStatus;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.usecases.encryption.UseCaseAdminFetchesEncryptionStatus;

@Service
public class AdministrationEncryptionStatusService {

    @Autowired
    DomainMessageService domainMessageService;

    @Autowired
    AuditLogService auditLogService;

    @UseCaseAdminFetchesEncryptionStatus(@Step(number = 1, name = "Service call", description = "Services collects encryption status from domains via event bus"))
    public SecHubEncryptionStatus fetchStatus() {
        auditLogService.log("starts collecting encryption status");

        SecHubEncryptionStatus sechubEncryptionStatus = new SecHubEncryptionStatus();
        collectScheduleEncryptionStatus(sechubEncryptionStatus);

        return sechubEncryptionStatus;

    }

    @IsSendingSyncMessage(MessageID.GET_ENCRYPTION_STATUS_SCHEDULE_DOMAIN)
    private void collectScheduleEncryptionStatus(SecHubEncryptionStatus status) {
        DomainMessage message = new DomainMessage(MessageID.GET_ENCRYPTION_STATUS_SCHEDULE_DOMAIN);

        DomainMessageSynchronousResult result = domainMessageService.sendSynchron(message);
        SecHubDomainEncryptionStatus schedulerStatus = result.get(MessageDataKeys.SECHUB_DOMAIN_ENCRYPTION_STATUS);

        status.getDomains().add(schedulerStatus);
    }

}
