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
import com.mercedesbenz.sechub.sharedkernel.usecases.encryption.UseCaseAdminStartsEncryptionRotation;

@Service
public class AdministrationEncryptionStatusService {

    @Autowired
    DomainMessageService domainMessageService;

    @Autowired
    AuditLogService auditLogService;

    @UseCaseAdminStartsEncryptionRotation(@Step(number = 2, name = "Service call", description = "Triggers rotation of encryption via domain message"))
    public SecHubEncryptionStatus fetchStatus() {
        auditLogService.log("starts collecting encryption status");

        SecHubEncryptionStatus status = new SecHubEncryptionStatus();
        addSchedulerStatus(status);

        return status;

    }

    @IsSendingSyncMessage(MessageID.GET_ENCRYPTION_STATUS_SCHEDULE_DOMAIN)
    private void addSchedulerStatus(SecHubEncryptionStatus status) {
        DomainMessage message = new DomainMessage(MessageID.GET_ENCRYPTION_STATUS_SCHEDULE_DOMAIN);

        DomainMessageSynchronousResult result = domainMessageService.sendSynchron(message);
        SecHubDomainEncryptionStatus schedulerStatus = result.get(MessageDataKeys.SECHUB_DOMAIN_ENCRYPTION_STATUS);

        status.getDomains().add(schedulerStatus);
    }

}
