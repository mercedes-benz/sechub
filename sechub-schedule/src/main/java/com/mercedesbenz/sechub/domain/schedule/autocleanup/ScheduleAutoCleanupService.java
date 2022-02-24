package com.mercedesbenz.sechub.domain.schedule.autocleanup;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.schedule.config.SchedulerConfigService;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.TimeCalculationService;
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

    @UseCaseScheduleAutoCleanExecution(@Step(number = 2, name = "Delete old data", description = "deletes old job information"))
    public void cleanup() {
        /* calculate */
        long days = configService.getAutoCleanupInDays();
        if (days == 0) {
            LOG.debug("Cancel schedule auto cleanup because disabled.");
            return;
        }
        LocalDateTime cleanTimeStamp = timeCalculationService.calculateNowMinusDays(days);

        /* delete */
        LOG.info("Do auto cleanup ScheduleSecHubJob. Everything older than {} days will be removed, means {}", days, cleanTimeStamp);
        jobRepository.deleteJobsOlderThan(cleanTimeStamp);

    }

}
