// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.administration.autocleanup.AdministrationAutoCleanupConfig;
import com.mercedesbenz.sechub.domain.administration.autocleanup.AdministrationAutoCleanupDaysCalculator;
import com.mercedesbenz.sechub.sharedkernel.SecHubEnvironment;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.AdministrationConfigMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesAutoCleanupConfiguration;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminUpdatesAutoCleanupConfiguration;
import com.mercedesbenz.sechub.sharedkernel.util.Assert;

@Service
public class AdministrationConfigService {

    @Autowired
    LogSanitizer logSanitizer;

    @Autowired
    AuditLogService auditLogService;

    @Autowired
    AdministrationConfigRepository repository;

    @Autowired
    AdministrationConfigTransactionService transactionService;

    @Autowired
    SecHubEnvironment environmentData;

    @Autowired
    AdministrationAutoCleanupDaysCalculator calculator;

    @Autowired
    @Lazy
    DomainMessageService domainMessageService;

    @UseCaseAdminUpdatesAutoCleanupConfiguration(@Step(number = 2, next = { 3, 4,
            5 }, name = "Updates auto cleanup config", description = "Updates auto cleanup configuration as JSON in database and sends event"))
    public void updateAutoCleanupConfiguration(AdministrationAutoCleanupConfig configuration) {
        Assert.notNull(configuration, "configuration may not be null");

        String configurationAsJson = configuration.toJSON();

        auditLogService.log("updates auto cleanup configuration to: {}", logSanitizer.sanitize(configurationAsJson, 8192));

        AdministrationConfig config = getOrCreateConfig();
        config.autoCleanupConfiguration = configurationAsJson;
        // store in own transaction, so never race conditions with events
        transactionService.saveConfigInOwnTransaction(config);

        sendEvent(configuration);
    }

    public long getAutoCleanupInDays() {
        AdministrationConfig config = getOrCreateConfig();
        return config.getAutoCleanupInDays();
    }

    @UseCaseAdminUpdatesAutoCleanupConfiguration(@Step(number = 3, name = "Updates auto cleanup in days settings", description = "Updates amount of days until cleanup in database by using received event data"))
    public void updateAutoCleanupInDays(long autoCleanupInDays) {
        AdministrationConfig config = getOrCreateConfig();
        config.autoCleanupInDays = autoCleanupInDays;

        // store in own transaction, so never race conditions with events
        transactionService.saveConfigInOwnTransaction(config);
    }

    @UseCaseAdminFetchesAutoCleanupConfiguration(@Step(number = 2, name = "Fetches auto cleanup config", description = "Fetches auto cleanup configuration from database"))
    public AdministrationAutoCleanupConfig fetchAutoCleanupConfiguration() {
        String cleanupConfigJson = getOrCreateConfig().autoCleanupConfiguration;
        AdministrationAutoCleanupConfig cleanupConfig = null;
        if (cleanupConfigJson == null) {
            cleanupConfig = new AdministrationAutoCleanupConfig();
        } else {
            cleanupConfig = AdministrationAutoCleanupConfig.fromString(cleanupConfigJson);
        }
        return cleanupConfig;
    }

    private void sendEvent(AdministrationAutoCleanupConfig config) {
        AdministrationConfigMessage adminConfigMessage = new AdministrationConfigMessage();
        adminConfigMessage.setAutoCleanupInDays(calculator.calculateCleanupTimeInDays(config));

        DomainMessage domainMessage = DomainMessageFactory.createEmptyRequest(MessageID.AUTO_CLEANUP_CONFIGURATION_CHANGED);
        domainMessage.set(MessageDataKeys.AUTO_CLEANUP_CONFIG_CHANGE_DATA, adminConfigMessage);

        domainMessageService.sendAsynchron(domainMessage);
    }

    private AdministrationConfig getOrCreateConfig() {
        Optional<AdministrationConfig> config = repository.findById(AdministrationConfig.ID);
        if (config.isPresent()) {
            return config.get();
        }
        AdministrationConfig newConfig = new AdministrationConfig();
        newConfig.autoCleanupConfiguration = new AdministrationAutoCleanupConfig().toJSON();
        return transactionService.saveConfigInOwnTransaction(newConfig);
    }
}
