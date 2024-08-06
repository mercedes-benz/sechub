// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.autocleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.pds.autocleanup.PDSAutoCleanupConfig.CleanupTime;

@Component
public class PDSAutoCleanupDaysCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(PDSAutoCleanupDaysCalculator.class);

    /**
     * Calculates cleanup time in days
     *
     * @param configuration
     * @return cleanup time in days
     */
    public long calculateCleanupTimeInDays(PDSAutoCleanupConfig config) {
        if (config == null) {
            LOG.warn("Given auto cleanup configuration was null! So use fallback configuration with defaults");
            config = new PDSAutoCleanupConfig();
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
