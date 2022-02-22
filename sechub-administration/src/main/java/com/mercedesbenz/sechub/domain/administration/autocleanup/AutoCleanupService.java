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
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdministrationAutoCleanExecution;

@Service
public class AutoCleanupService {

    @Autowired
    TimeCalculationService timeCalculationService;

    @Autowired
    AdministrationConfigService configService;

    @Autowired
    JobInformationRepository jobInformationRepository;

    private static final Logger LOG = LoggerFactory.getLogger(AutoCleanupService.class);

    @UseCaseAdministrationAutoCleanExecution(@Step(number = 2, name = "Delete old data", description = "deletes old job information"))
    public void cleanup() {
        /* calculate */
        long days = configService.getAutoCleanupInDays();
        LocalDateTime cleanTimeStamp = timeCalculationService.calculateNowMinusDays(days);

        /* delete */
        LOG.info("Do auto cleanup JobInformation. Everything older than {} days will be removed, means {}", days, cleanTimeStamp);
        jobInformationRepository.deleteJobInformationOlderThan(cleanTimeStamp);

    }

}
