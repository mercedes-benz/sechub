package com.mercedesbenz.sechub.domain.administration.autocleanup;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.administration.config.AdministrationConfigService;
import com.mercedesbenz.sechub.domain.administration.job.JobInformationRepository;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.TimeCalculationService;
import com.mercedesbenz.sechub.sharedkernel.autocleanup.AutoCleanupResult;
import com.mercedesbenz.sechub.sharedkernel.autocleanup.AutoCleanupResultInspector;
import com.mercedesbenz.sechub.sharedkernel.usecases.autocleanup.UseCaseAdministrationAutoCleanExecution;

@Service
public class AdministrationAutoCleanupService {

    private static final Logger LOG = LoggerFactory.getLogger(AdministrationAutoCleanupService.class);

    @Autowired
    AutoCleanupResultInspector inspector;

    @Autowired
    TimeCalculationService timeCalculationService;

    @Autowired
    AdministrationConfigService configService;

    @Autowired
    JobInformationRepository jobInformationRepository;

    @UseCaseAdministrationAutoCleanExecution(@Step(number = 2, name = "Delete old data", description = "deletes old job information"))
    public void cleanup() {
        /* calculate */
        long days = configService.getAutoCleanupInDays();

        if (days < 0) {
            LOG.error("Found {} days configured for auto cleanup. Negative values are not allowed. Please check your configuration.", days);
            return;
        }
        if (days == 0) {
            LOG.trace("Cancel administration auto cleanup because disabled.");
            return;
        }
        LocalDateTime cleanTimeStamp = timeCalculationService.calculateNowMinusDays(days);

        /* delete */
        int amount = jobInformationRepository.deleteJobInformationOlderThan(cleanTimeStamp);

        /* @formatter:off */
        inspector.inspect(AutoCleanupResult.builder().
                    autoCleanup("job-information",getClass()).
                    forDays(days).
                    hasDeleted(amount).
                    byTimeStamp(cleanTimeStamp).
                    build()
                    );
        /* @formatter:on */

    }

}
