package com.mercedesbenz.sechub.domain.administration.autocleanup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.administration.config.AdministrationConfigService;
import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseAutoCleanCheckStarting;

@Service
public class AutoCleanupTriggerService {
    private static final int DEFAULT_INITIAL_DELAY_MILLIS = 5000;
    private static final int DEFAULT_FIXED_DELAY_MILLIS = 10000;

    @Autowired
    AdministrationConfigService configService;

    @Autowired
    AutoCleanupService autoCleanupService;

    // default 10 seconds delay and 5 seconds initial
    @MustBeDocumented("Auto cleanup is triggered by a cron job operation - default is 10 seconds to delay after last execution. " + "The initial delay is "
            + DEFAULT_INITIAL_DELAY_MILLIS + " milliseconds is defined. It can be configured different,so when you need to startup a cluster "
            + "time shifted, simply change the initial delay values in your wanted way.")
    @Scheduled(initialDelayString = "${sechub.config.trigger.autoclean.initialdelay:" + DEFAULT_INITIAL_DELAY_MILLIS
            + "}", fixedDelayString = "${sechub.config.trigger.autoclean.delay:" + DEFAULT_FIXED_DELAY_MILLIS + "}")
    @UseCaseAutoCleanCheckStarting(@Step(number = 1, name = "Scheduling", description = "Checks for parts to auto clean."))
    public void triggerAutoCleanup() {
        AutoCleanupConfig configuration = configService.fetchAutoCleanupConfiguration();
        autoCleanupService.cleanup(configuration);
    }
}
