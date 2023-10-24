// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.autocleanup;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.schedule.config.SchedulerConfigService;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobDataRepository;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.TimeCalculationService;
import com.mercedesbenz.sechub.sharedkernel.autocleanup.AutoCleanupResult;
import com.mercedesbenz.sechub.sharedkernel.autocleanup.AutoCleanupResultInspector;
import com.mercedesbenz.sechub.sharedkernel.usecases.autocleanup.UseCaseScheduleAutoCleanExecution;

@Service
public class ScheduleAutoCleanupService {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleAutoCleanupService.class);

    @Autowired
    TimeCalculationService timeCalculationService;

    @Autowired
    SchedulerConfigService configService;

    @Autowired
    SecHubJobRepository jobRepository;

    @Autowired
    SecHubJobDataRepository jobDataRepository;

    @Autowired
    AutoCleanupResultInspector inspector;

    @UseCaseScheduleAutoCleanExecution(@Step(number = 2, name = "Delete old data", description = "deletes old job information"))
    public void cleanup() {
        /* calculate */
        long days = configService.getAutoCleanupInDays();
        if (days == 0) {
            LOG.trace("Cancel schedule auto cleanup because disabled.");
            return;
        }
        LocalDateTime cleanTimeStamp = timeCalculationService.calculateNowMinusDays(days);

        /* delete */
        int amount = jobRepository.deleteJobsOlderThan(cleanTimeStamp);
        jobDataRepository.deleteJobDataOlderThan(cleanTimeStamp);

        /* @formatter:off */
        inspector.inspect(AutoCleanupResult.builder().
                    autoCleanup("sechub-jobs",getClass()).
                    forDays(days).
                    hasDeleted(amount).
                    byTimeStamp(cleanTimeStamp).
                    build()
                    );
        /* @formatter:on */

    }

}
