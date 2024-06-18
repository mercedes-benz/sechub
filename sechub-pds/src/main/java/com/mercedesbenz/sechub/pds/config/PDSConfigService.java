// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.config;

import static com.mercedesbenz.sechub.pds.autocleanup.PDSAutoCleanupConstants.*;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.mercedesbenz.sechub.pds.PDSNotAcceptableException;
import com.mercedesbenz.sechub.pds.autocleanup.PDSAutoCleanupConfig;
import com.mercedesbenz.sechub.pds.autocleanup.PDSAutoCleanupConfig.CleanupTime;
import com.mercedesbenz.sechub.pds.autocleanup.PDSAutoCleanupDaysCalculator;
import com.mercedesbenz.sechub.pds.commons.core.PDSLogSanitizer;
import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.UseCaseAdminFetchesAutoCleanupConfiguration;
import com.mercedesbenz.sechub.pds.usecase.UseCaseAdminUpdatesAutoCleanupConfiguration;

@Service
public class PDSConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSConfigService.class);

    @Autowired
    PDSLogSanitizer pdsLogSanitizer;

    @Autowired
    PDSConfigRepository repository;

    @Autowired
    PDSConfigTransactionService transactionService;

    @Autowired
    PDSAutoCleanupDaysCalculator calculator;

    @UseCaseAdminUpdatesAutoCleanupConfiguration(@PDSStep(number = 2, next = { 3, 4,
            5 }, name = "Updates auto cleanup config", description = "Updates auto cleanup configuration as JSON in database"))
    public void updateAutoCleanupConfiguration(PDSAutoCleanupConfig configuration) {
        Assert.notNull(configuration, "configuration may not be null");

        long calculateCleanupTimeInDays = calculator.calculateCleanupTimeInDays(configuration);
        if (calculateCleanupTimeInDays < 0) {
            throw new PDSNotAcceptableException("Negative cleanup time is not accepted!");
        }

        String configurationAsJson = configuration.toJSON();

        LOG.info("Admin updates auto cleanup configuration to: {}", pdsLogSanitizer.sanitize(configurationAsJson, 8192));

        PDSConfig config = getOrCreateConfig();
        config.autoCleanupConfiguration = configurationAsJson;
        config.autoCleanupInDays = calculateCleanupTimeInDays;
        // store in own transaction, so never race conditions with events
        transactionService.saveConfigInOwnTransaction(config);
    }

    public long getAutoCleanupInDays() {
        PDSConfig config = getOrCreateConfig();
        return config.getAutoCleanupInDays();
    }

    @UseCaseAdminUpdatesAutoCleanupConfiguration(@PDSStep(number = 3, name = "Calculate auto cleanup days", description = "After receiving the new cleanup configuration as JSON the cleanup days will be calculated and persisted as well"))
    public void updateAutoCleanupInDays(long autoCleanupInDays) {
        PDSConfig config = getOrCreateConfig();
        config.autoCleanupInDays = autoCleanupInDays;

        // store in own transaction, so never race conditions with events
        transactionService.saveConfigInOwnTransaction(config);
    }

    @UseCaseAdminFetchesAutoCleanupConfiguration(@PDSStep(number = 2, name = "Fetches auto cleanup config", description = "Fetches auto cleanup configuration from database"))
    public PDSAutoCleanupConfig fetchAutoCleanupConfiguration() {
        String cleanupConfigJson = getOrCreateConfig().autoCleanupConfiguration;
        PDSAutoCleanupConfig cleanupConfig = null;
        if (cleanupConfigJson == null) {
            cleanupConfig = new PDSAutoCleanupConfig();
        } else {
            cleanupConfig = PDSAutoCleanupConfig.fromString(cleanupConfigJson);
        }
        return cleanupConfig;
    }

    private PDSConfig getOrCreateConfig() {
        Optional<PDSConfig> config = repository.findById(PDSConfig.ID);
        if (config.isPresent()) {
            return config.get();
        }
        PDSAutoCleanupConfig autoCleanupConfiguration = createDefaultAutoCleanupConfig();

        PDSConfig newConfig = new PDSConfig();
        newConfig.autoCleanupConfiguration = autoCleanupConfiguration.toJSON();
        newConfig.autoCleanupInDays = calculator.calculateCleanupTimeInDays(autoCleanupConfiguration);

        return transactionService.saveConfigInOwnTransaction(newConfig);
    }

    private PDSAutoCleanupConfig createDefaultAutoCleanupConfig() {
        PDSAutoCleanupConfig autoCleanupConfiguration = new PDSAutoCleanupConfig();
        CleanupTime cleanupTime = autoCleanupConfiguration.getCleanupTime();
        cleanupTime.setAmount(DEFAULT_AUTO_CLEANUP_CONFIG_AMOUNT);
        cleanupTime.setUnit(DEFAULT_AUTO_CLEANUP_CONFIG_UNIT);
        return autoCleanupConfiguration;
    }
}
