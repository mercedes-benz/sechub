// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionData;
import com.mercedesbenz.sechub.sharedkernel.usecases.encryption.UseCaseAdminStartsEncryptionRotation;

@Service
public class ScheduleEncryptionRotationService {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleEncryptionRotationService.class);

    @Autowired
    ScheduleCipherPoolDataTransactionService transactionService;

    @Autowired
    ScheduleEncryptionService encryptionService;

    @UseCaseAdminStartsEncryptionRotation(@Step(number = 3, name = "Service call", description = "Forces new cipher pool entry creation and triggers encryption service pool refresh"))
    public void startEncryptionRotation(SecHubEncryptionData data, String executedBy) {
        /* first create new cipher pool entry */
        try {

            LOG.info("start rotation encryption");

            String testText = UUID.randomUUID().toString();

            ScheduleCipherPoolData poolData = encryptionService.createInitialCipherPoolData(data, testText);
            poolData.createdFrom = executedBy;

            ScheduleCipherPoolData newCreatedEntry = transactionService.storeInOwnTransaction(poolData);

            LOG.info("Created new cipher pool entry with id: {}, algorithm: {}, creation timestamp: {}, created from: {} ", newCreatedEntry.id,
                    newCreatedEntry.algorithm, newCreatedEntry.created, newCreatedEntry.createdFrom);
        } catch (ScheduleEncryptionException e) {
            LOG.error("Was not able to create new cipher pool entry!", e);
        }

        LOG.info("Trigger refresh of encryption pool in this instance");
        encryptionService.refreshEncryptionPoolAndLatestPoolIdIfNecessary();

    }

}
