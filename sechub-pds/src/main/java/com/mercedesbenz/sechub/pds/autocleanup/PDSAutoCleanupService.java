// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.autocleanup;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.pds.config.PDSConfigService;
import com.mercedesbenz.sechub.pds.job.PDSJobRepository;
import com.mercedesbenz.sechub.pds.time.PDSTimeCalculationService;
import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.UseCaseSystemExecutesAutoCleanup;

@Service
public class PDSAutoCleanupService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSAutoCleanupService.class);

    @Autowired
    PDSAutoCleanupResultInspector inspector;

    @Autowired
    PDSTimeCalculationService PDSTimeCalculationService;

    @Autowired
    PDSConfigService configService;

    @Autowired
    PDSJobRepository jobRepository;

    @UseCaseSystemExecutesAutoCleanup(@PDSStep(number = 2, name = "Delete old data", description = "deletes old PDS job data"))
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
        LocalDateTime cleanTimeStamp = PDSTimeCalculationService.calculateNowMinusDays(days);

        /* delete */
        int amount = jobRepository.deleteJobOlderThan(cleanTimeStamp);

        /* @formatter:off */
        inspector.inspect(PDSAutoCleanupResult.builder().
                    autoCleanup("pds-job",getClass()).
                    forDays(days).
                    hasDeleted(amount).
                    byTimeStamp(cleanTimeStamp).
                    build()
                    );
        /* @formatter:on */

    }

}
