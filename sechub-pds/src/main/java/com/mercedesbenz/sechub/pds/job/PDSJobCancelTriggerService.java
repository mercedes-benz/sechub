// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.pds.PDSMustBeDocumented;
import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.UseCaseSystemHandlesJobCancelRequests;

@Service
public class PDSJobCancelTriggerService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSJobCancelTriggerService.class);

    private static final int DEFAULT_INITIAL_DELAY_MILLIS = 5 * 1000; // 5 seconds delay
    private static final int DEFAULT_FIXED_DELAY_MILLIS = 10 * 1000; // every ten seconds

    private static final String TRIGGER_STEP_MUST_BE_DOCUMENTED = "Cancellation is triggered by a cron job operation." + "As initial delay "
            + DEFAULT_INITIAL_DELAY_MILLIS
            + " milliseconds are defined. It can be configured differently. This is useful when you need to startup a cluster. Simply use some different values for the cluster members, this limit concurrent access.";

    private static final String TRIGGER_INITIAL_DELAY_STRING = "${pds.config.trigger.jobcancellation.initialdelay:" + DEFAULT_INITIAL_DELAY_MILLIS + "}";
    private static final String TRIGGER_FIXED_DELAY_STRING = "${pds.config.trigger.jobcancellation.delay:" + DEFAULT_FIXED_DELAY_MILLIS + "}";

    @Autowired
    PDSCancelService cancelService;

    @PDSMustBeDocumented(TRIGGER_STEP_MUST_BE_DOCUMENTED)
    @Scheduled(initialDelayString = TRIGGER_INITIAL_DELAY_STRING, fixedDelayString = TRIGGER_FIXED_DELAY_STRING)
    @UseCaseSystemHandlesJobCancelRequests(@PDSStep(number = 1, name = "Scheduling", description = "Checks for PDS job cancel requests."))
    public void triggerHandleCancelRequests() {
        LOG.trace("Trigger cancel request handling started");

        cancelService.handleJobCancelRequests();
    }
}
