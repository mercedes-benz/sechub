package com.mercedesbenz.sechub.domain.schedule.encryption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.encryption.EncryptionSupport;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionData;
import com.mercedesbenz.sechub.sharedkernel.usecases.encryption.UseCaseAdminStartsEncryptionRotation;

@Service
public class ScheduleEncryptionRotationService {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleEncryptionRotationService.class);

    @Autowired
    EncryptionSupport support;

    @Autowired
    ScheduleCipherPoolDataRepository repository;

    @UseCaseAdminStartsEncryptionRotation(@Step(number = 3, name = "Service call", description = "Rotates scheduler encryption by creating a new cipher pool entry"))
    public void rotateEncryption(SecHubEncryptionData data) {
        LOG.info("start rotation encryption");

        /*
         * create new entry in database - the
         * ScheduleRefreshEncryptionServiceSetupTriggerService will then start the
         * update process on every cluster member and a new encryption pool will be
         * created.
         *
         * After the new pool is active, the
         */
        ScheduleCipherPoolData poolData = new ScheduleCipherPoolData();
        repository.save(poolData);

    }

}
