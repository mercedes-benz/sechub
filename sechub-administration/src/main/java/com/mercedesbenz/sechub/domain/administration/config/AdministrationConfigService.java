// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.administration.autocleanup.AutoCleanupConfig;
import com.mercedesbenz.sechub.domain.administration.autocleanup.AutoCleanupDaysCalculator;
import com.mercedesbenz.sechub.sharedkernel.SecHubEnvironment;
import com.mercedesbenz.sechub.sharedkernel.Step;
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
    AdministrationConfigRepository repository;

    @Autowired
    AdministrationConfigTransactionService transactionService;

    @Autowired
    SecHubEnvironment environmentData;

    @Autowired
    AutoCleanupDaysCalculator calculator;

    @Autowired
    @Lazy
    DomainMessageService domainMessageService;

    @UseCaseAdminUpdatesAutoCleanupConfiguration(@Step(number = 2, name = "Updates auto cleanup config", description = "Updates auto cleanup configuration in database"))
    public void updateAutoCleanup(AutoCleanupConfig configuration) {
        Assert.notNull(configuration, "configuration may not be null");

        AdministrationConfig config = getOrCreateConfig();
        config.autoCleanupConfiguration = configuration.toJSON();
        // store in own transaction, so never race conditions with events
        transactionService.saveConfigInOwnTransaction(config);

        sendEvent(configuration);
    }

    @UseCaseAdminFetchesAutoCleanupConfiguration(@Step(number = 2, name = "Fetches auto cleanup config", description = "Fetches auto cleanup configuration from database"))
    public AutoCleanupConfig fetchAutoCleanupConfiguration() {
        String cleanupConfigJson = getOrCreateConfig().autoCleanupConfiguration;
        AutoCleanupConfig cleanupConfig = null;
        if (cleanupConfigJson == null) {
            cleanupConfig = new AutoCleanupConfig();
        } else {
            cleanupConfig = AutoCleanupConfig.fromString(cleanupConfigJson);
        }
        return cleanupConfig;
    }

    private void sendEvent(AutoCleanupConfig config) {
        AdministrationConfigMessage adminConfigMessage = new AdministrationConfigMessage();
        adminConfigMessage.setDaysBeforeAutoCleanup(calculator.calculateCleanupTimeInDays(config));

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
        newConfig.autoCleanupConfiguration = new AutoCleanupConfig().toJSON();
        return transactionService.saveConfigInOwnTransaction(newConfig);
    }
}
