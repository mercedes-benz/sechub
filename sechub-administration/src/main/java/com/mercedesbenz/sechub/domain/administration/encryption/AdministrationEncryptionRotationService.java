// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.encryption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionData;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionDataValidator;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.security.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.usecases.encryption.UseCaseAdminStartsEncryptionRotation;

@Service
public class AdministrationEncryptionRotationService {

    @Autowired
    DomainMessageService domainMessageService;

    @Autowired
    SecHubEncryptionDataValidator validator;

    @Autowired
    AuditLogService auditLogService;

    @Autowired
    UserContextService userContextService;

    @UseCaseAdminStartsEncryptionRotation(@Step(number = 2, name = "Service call", description = "Triggers rotation of encryption via domain message"))
    @IsSendingAsyncMessage(MessageID.START_ENCRYPTION_ROTATION)
    public void rotateEncryption(SecHubEncryptionData data) {
        if (data == null) {
            throw new IllegalArgumentException("data may not be null!");
        }
        auditLogService.log("started encryption rotation. New cipher algorithm will be: {}, datasource type:{}, datasource: {}", data.getAlgorithm(),
                data.getPasswordSourceType(), data.getPasswordSourceData());

        String executedBy = userContextService.getUserId();

        DomainMessage message = new DomainMessage(MessageID.START_ENCRYPTION_ROTATION);
        message.set(MessageDataKeys.SECHUB_ENCRYPT_ROTATION_DATA, data);
        message.set(MessageDataKeys.EXECUTED_BY, executedBy);

        domainMessageService.sendAsynchron(message);
    }

}
