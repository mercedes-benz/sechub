package com.mercedesbenz.sechub.domain.administration.autocleanup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdministrationAutoCleanExecution;

@Service
public class AdministrationAutoCleanupTriggerService {

    private static final int DEFAULT_INITIAL_DELAY_MILLIS = 5 * 1000; // 5 seconds delay
    private static final int DEFAULT_FIXED_DELAY_MILLIS = 24 * 60 * 60 * 1000; // one day

    @Autowired
    AdministrationAutoCleanupService autoCleanupService;

    // default 10 seconds delay and 5 seconds initial
    @MustBeDocumented("Auto cleanup is triggered by a cron job operation - default is one day to delay after last execution. " + "The initial delay is "
            + DEFAULT_INITIAL_DELAY_MILLIS + " milliseconds is defined. It can be configured different,so when you need to startup a cluster "
            + "time shifted, simply change the initial delay values in your wanted way.")
    @Scheduled(initialDelayString = "${sechub.config.trigger.autoclean.initialdelay:" + DEFAULT_INITIAL_DELAY_MILLIS
            + "}", fixedDelayString = "${sechub.config.trigger.autoclean.delay:" + DEFAULT_FIXED_DELAY_MILLIS + "}")
    @UseCaseAdministrationAutoCleanExecution(@Step(number = 1, name = "Scheduling", description = "Checks for parts to auto clean."))
    public void triggerAutoCleanup() {
        autoCleanupService.cleanup();
    }
}
