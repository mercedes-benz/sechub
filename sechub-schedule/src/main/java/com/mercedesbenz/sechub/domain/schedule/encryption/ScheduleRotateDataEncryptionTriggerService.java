// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import static com.mercedesbenz.sechub.sharedkernel.autocleanup.AutoCleanupConstants.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJobEncryptionUpdateService;
import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.encryption.UseCaseScheduleRotateDataEncryption;

/**
 * This service does periodically check for encrypted data which uses older
 * cipher. For those where cipher is out dated, the data encryption will be
 * rotated.
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class ScheduleRotateDataEncryptionTriggerService {

    private static final int DEFAULT_INITIAL_DELAY_MILLIS = 5 * 1000; // 5 seconds delay
    private static final int DEFAULT_FIXED_DELAY_MILLIS = 5 * 60 * 1000; // 5 minutes

    private static final String INITIAL_DELAY_STRING = "${sechub.config.trigger.refresh.encrypteddata.initialdelay:" + DEFAULT_INITIAL_DELAY_MILLIS + "}";
    private static final String FIXED_DELAY_STRING = "${sechub.config.trigger.refresh.encrypteddata.delay" + DEFAULT_FIXED_DELAY_MILLIS + "}";

    private static final String DESCRIPTION = "Scheduler instance will update periodically encrypted data where data is encrypted with old cipher.";

    @Autowired
    ScheduleSecHubJobEncryptionUpdateService updateService;

    @Autowired
    ScheduleLatestCipherPoolIdResolver latestCipherPoolidResolver;

    @MustBeDocumented(TRIGGER_STEP_MUST_BE_DOCUMENTED)
    @Scheduled(initialDelayString = INITIAL_DELAY_STRING, fixedDelayString = FIXED_DELAY_STRING)
    @UseCaseScheduleRotateDataEncryption(@Step(number = 1, name = "Encryption pool data refresh trigger", description = DESCRIPTION))
    public void triggerUpdateOfEncryptedData() {
        updateService.updateEncryptionData(10);
    }
}
