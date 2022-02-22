package com.mercedesbenz.sechub.domain.administration.autocleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.domain.administration.autocleanup.AutoCleanupConfig.CleanupTime;

@Component
public class AutoCleanupDaysCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(AutoCleanupDaysCalculator.class);

    /**
     * Calculates cleanup time in days
     *
     * @param config
     * @return cleanup time in days
     */
    public long calculateCleanupTimeInDays(AutoCleanupConfig config) {
        if (config == null) {
            LOG.warn("Given auto cleanup configuration was null! So use fallback configuration with defaults");
            config = new AutoCleanupConfig();
        }
        CleanupTime time = config.getCleanupTime();
        long multiplicator = time.getUnit().getMultiplicatorDays();
        int amount = time.getAmount();
        if (amount < 1) {
            amount = 1;
            LOG.warn("Configured amount for cleanup was smaller than 1 so fallback to 1");
        }
        long cleanupTimeInDays = amount * multiplicator;
        return cleanupTimeInDays;
    }

}
