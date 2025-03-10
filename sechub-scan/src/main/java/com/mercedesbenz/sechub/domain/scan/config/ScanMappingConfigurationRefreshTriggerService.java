// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.DocumentationScopeConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdmiUpdatesMappingConfiguration;

@Service
public class ScanMappingConfigurationRefreshTriggerService {

    private static final int DEFAULT_INITIAL_DELAY_MILLIS = 0;
    private static final int DEFAULT_FIXED_DELAY_MILLIS = 5000;

    @MustBeDocumented(value = "Define initial delay (in milliseconds) for scan config refresh check operation.", scope = DocumentationScopeConstants.SCOPE_SCAN)
    @Value("${sechub.config.scan.scanconfig.refresh.initialdelay:" + DEFAULT_INITIAL_DELAY_MILLIS + "}")
    private String initialDelay; // here only for logging - used in scheduler annotation as well!

    @MustBeDocumented(value = "Define delay (in milliseconds) for next job execution trigger after last executed.", scope = DocumentationScopeConstants.SCOPE_SCAN)
    @Value("${sechub.config.scan.scanconfig.refresh.delay:" + DEFAULT_FIXED_DELAY_MILLIS + "}")
    private String fixedDelay; // here only for logging - used in scheduler annotation as well!

    @Autowired
    ScanMappingConfigurationService scanMappingConfigurationService;

    @UseCaseAdmiUpdatesMappingConfiguration(@Step(number = 5, name = "Trigger service", description = "Checks periodically for updates in scan configuration"))
    @Scheduled(initialDelayString = "${sechub.config.trigger.nextjob.initialdelay:" + DEFAULT_INITIAL_DELAY_MILLIS
            + "}", fixedDelayString = "${sechub.config.scan.scanconfig.refresh.delay:" + DEFAULT_FIXED_DELAY_MILLIS + "}")
    public void triggerRefreshCheck() {
        scanMappingConfigurationService.refreshScanConfigIfNecessary();
    }
}
