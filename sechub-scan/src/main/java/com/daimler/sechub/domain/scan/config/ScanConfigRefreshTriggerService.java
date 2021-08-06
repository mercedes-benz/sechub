// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.MustBeDocumented;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdmiUpdatesMappingConfiguration;

@Service
public class ScanConfigRefreshTriggerService {

    private static final int DEFAULT_INITIAL_DELAY_MILLIS = 0;
    private static final int DEFAULT_FIXED_DELAY_MILLIS = 5000;

    @MustBeDocumented("Define initial delay (in milliseconds) for scan config refresh check operation.")
    @Value("${sechub.config.scan.scanconfig.refresh.initialdelay:" + DEFAULT_INITIAL_DELAY_MILLIS + "}")
    private String initialDelay; // here only for logging - used in scheduler annotation as well!

    @MustBeDocumented("Define delay (in milliseconds) for next job execution trigger after last executed.")
    @Value("${sechub.config.scan.scanconfig.refresh.delay:" + DEFAULT_FIXED_DELAY_MILLIS + "}")
    private String fixedDelay; // here only for logging - used in scheduler annotation as well!
    
    @Autowired
    ScanConfigService scanConfigService;
    
    @UseCaseAdmiUpdatesMappingConfiguration(@Step(number=5,name="Trigger service",description="Checks periodically for updates in scan configuration"))
    @Scheduled(initialDelayString = "${sechub.config.trigger.nextjob.initialdelay:" + DEFAULT_INITIAL_DELAY_MILLIS
            + "}", fixedDelayString = "${sechub.config.scan.scanconfig.refresh.delay:" + DEFAULT_FIXED_DELAY_MILLIS + "}")
    public void triggerRefreshCheck() {
        scanConfigService.refreshScanConfigIfNecessary();
    }
}
