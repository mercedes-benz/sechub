// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.encryption.UseCaseScheduleEncryptionPoolRefresh;

/**
 * This service is responsible to trigger periodically refresh checks on the
 * encryption service to update encryption pool and latest pool id when
 * necessary.
 *
 * The reason for the periodic check is that we do not want to check for latest
 * cipher etc. on every job creation or every encryption rotation - we separated
 * the pool refresh and the encryption/re-encryption (which will always use the
 * encryption pool and its information).
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class ScheduleRefreshEncryptionServiceSetupTriggerService {

    private static final int DEFAULT_INITIAL_DELAY_MILLIS = 5 * 1000; // 5 seconds delay
    private static final int DEFAULT_FIXED_DELAY_MILLIS = 5 * 60 * 1000; // 5 minutes

    static final String SPRING_VALUE_INITIAL_DELAY_MILLISECONDS = "${sechub.schedule.encryption.refresh.initialdelay:" + DEFAULT_INITIAL_DELAY_MILLIS + "}";
    static final String SPRING_VALUE_FIXED_DELAY_MILLISECONDS = "${sechub.schedule.encryption.refresh.delay:" + DEFAULT_FIXED_DELAY_MILLIS + "}";

    private static final String DESCRIPTION = "Scheduler instance will check if encryption pool is in sync with the database definitions. If not, the instance will try to create new encryption pool object and provide the new setup.";

    @Autowired
    ScheduleEncryptionService encryptionService;

    @MustBeDocumented("Defines the initial and also the fixed delay for the refresh interval. These values are also used for calculation of remaining run time of outdated encrytion pools (when refresh fails)")
    @Scheduled(initialDelayString = SPRING_VALUE_INITIAL_DELAY_MILLISECONDS, fixedDelayString = SPRING_VALUE_FIXED_DELAY_MILLISECONDS)
    @UseCaseScheduleEncryptionPoolRefresh(@Step(number = 1, name = "Encryption pool data refresh trigger", description = DESCRIPTION))
    public void triggerEncryptionSetupRefresh() {
        encryptionService.refreshEncryptionPoolAndLatestPoolIdIfNecessary();
    }
}
