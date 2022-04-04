// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminUpdatesAutoCleanupConfiguration;

@Service
public class ScanConfigService {

    @Autowired
    ScanConfigRepository repository;

    @Autowired
    ScanConfigTransactionService transactionService;

    public long getAutoCleanupInDays() {
        ScanConfig config = getOrCreateConfig();
        return config.getAutoCleanupInDays();
    }

    private ScanConfig getOrCreateConfig() {
        Optional<ScanConfig> config = repository.findById(ScanConfig.ID);
        if (config.isPresent()) {
            return config.get();
        }
        ScanConfig newConfig = new ScanConfig();
        return transactionService.saveConfigInOwnTransaction(newConfig);
    }

    @UseCaseAdminUpdatesAutoCleanupConfiguration(@Step(number = 5, name = "Scan domain receives auto cleanup event", description = "Received event in scan domain about auto cleanup configuration change. Stores data, so available for next auto clean execution"))
    public void updateAutoCleanupInDays(long autoCleanupInDays) {
        ScanConfig config = getOrCreateConfig();
        config.autoCleanupInDays = autoCleanupInDays;

        repository.save(config);

    }
}
