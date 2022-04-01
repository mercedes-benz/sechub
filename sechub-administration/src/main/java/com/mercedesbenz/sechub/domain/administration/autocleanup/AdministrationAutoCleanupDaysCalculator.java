// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.autocleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.domain.administration.autocleanup.AdministrationAutoCleanupConfig.CleanupTime;

@Component
public class AdministrationAutoCleanupDaysCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(AdministrationAutoCleanupDaysCalculator.class);

    /**
     * Calculates cleanup time in days
     *
     * @param config
     * @return cleanup time in days
     */
    public long calculateCleanupTimeInDays(AdministrationAutoCleanupConfig config) {
        if (config == null) {
            LOG.warn("Given auto cleanup configuration was null! So use fallback configuration with defaults");
            config = new AdministrationAutoCleanupConfig();
        }
        CleanupTime time = config.getCleanupTime();
        long multiplicator = time.getUnit().getMultiplicatorDays();
        int amount = time.getAmount();
        if (amount < 0) {
            amount = 0;
            LOG.warn("Configured amount for cleanup was smaller than 0 so fallback to 0");
        }
        long cleanupTimeInDays = amount * multiplicator;
        return cleanupTimeInDays;
    }

}
