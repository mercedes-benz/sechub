// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.batch;

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.sechub.pds.PDSMustBeDocumented;
import com.daimler.sechub.pds.execution.PDSExecutionService;
import com.daimler.sechub.pds.job.PDSJob;
import com.daimler.sechub.pds.job.PDSJobRepository;
import com.daimler.sechub.pds.job.PDSJobStatusState;

@Service
public class PDSBatchTriggerService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSBatchTriggerService.class);

    private static final int DEFAULT_INITIAL_DELAY_MILLIS = 3000;
    private static final int DEFAULT_FIXED_DELAY_MILLIS = 5000;

    private static final boolean DEFAULT_SCHEDULING_ENABLED = true;

    @Autowired
    PDSExecutionService executionService;

    @Autowired
    PDSJobRepository repository;

    @PDSMustBeDocumented(value="initial delay for next job trigger in milliseconds",scope="scheduler")
    @Value("${sechub.pds.config.trigger.nextjob.initialdelay:" + DEFAULT_INITIAL_DELAY_MILLIS + "}")
    private String infoInitialDelay; // here only for logging - used in scheduler annotation as well!

    @PDSMustBeDocumented(value="delay for next job trigger in milliseconds",scope="scheduler")
    @Value("${sechub.pds.config.trigger.nextjob.delay:" + DEFAULT_FIXED_DELAY_MILLIS + "}")
    private String infoFixedDelay; // here only for logging - used in scheduler annotation as well!

    @PDSMustBeDocumented(value="Set scheduler enabled state",scope="scheduler")
    @Value("${sechub.pds.config.scheduling.enable:"+DEFAULT_SCHEDULING_ENABLED+"}")
    boolean schedulingEnabled=DEFAULT_SCHEDULING_ENABLED;

    @PostConstruct
    protected void postConstruct() {
        // show info about delay values in log (once)
        LOG.info("Scheduler service created with {} millisecondss initial delay and {} millisecondss as fixed delay", infoInitialDelay, infoFixedDelay);
    }

    // default 10 seconds delay and 5 seconds initial
    @Scheduled(initialDelayString = "${sechub.pds.config.trigger.nextjob.initialdelay:" + DEFAULT_INITIAL_DELAY_MILLIS
            + "}", fixedDelayString = "${sechub.pds.config.trigger.nextjob.delay:" + DEFAULT_FIXED_DELAY_MILLIS + "}")
    @Transactional
    public void triggerExecutionOfNextJob() {
        if (!schedulingEnabled) {
            LOG.trace("Trigger execution of next job canceled, because scheduling disabled.");
            return;
        }
        LOG.trace("Trigger execution of next job started.");
        if (executionService.isQueueFull()) {
            LOG.debug("Execution service is not able to execute next job, so cancel here");
            return;
        }
        /* query does auto increment version here! */
        Optional<PDSJob> nextJob = repository.findNextJobToExecute();
        if (!nextJob.isPresent()) {
            LOG.trace("No next job present");
            return;
        }
        PDSJob pdsJob = nextJob.get();
        pdsJob.setState(PDSJobStatusState.QUEUED);

        /*
         * next is done async - so on leave of this methods PDS job version will be
         * updated + state set to queue, so no other POD will process this job again
         */
        executionService.addToExecutionQueueAsynchron(pdsJob.getUUID());

    }

}