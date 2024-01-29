// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.batch;

import java.util.Random;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.pds.PDSMustBeDocumented;
import com.mercedesbenz.sechub.pds.execution.PDSExecutionService;
import com.mercedesbenz.sechub.pds.job.PDSJobRepository;
import com.mercedesbenz.sechub.pds.job.PDSJobTransactionService;

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

    @Autowired
    PDSJobTransactionService jobTransactionService;

    @PDSMustBeDocumented(value = "initial delay for next job trigger in milliseconds", scope = "scheduler")
    @Value("${pds.config.trigger.nextjob.initialdelay:" + DEFAULT_INITIAL_DELAY_MILLIS + "}")
    private String infoInitialDelay; // here only for logging - used in scheduler annotation as well!

    @PDSMustBeDocumented(value = "delay for next job trigger in milliseconds", scope = "scheduler")
    @Value("${pds.config.trigger.nextjob.delay:" + DEFAULT_FIXED_DELAY_MILLIS + "}")
    private String infoFixedDelay; // here only for logging - used in scheduler annotation as well!

    @PDSMustBeDocumented(value = "Set scheduler enabled state", scope = "scheduler")
    @Value("${pds.config.scheduling.enable:" + DEFAULT_SCHEDULING_ENABLED + "}")
    boolean schedulingEnabled = DEFAULT_SCHEDULING_ENABLED;

    @PostConstruct
    protected void postConstruct() {
        // show info about delay values in log (once)
        LOG.info("Scheduler service created with {} millisecondss initial delay and {} millisecondss as fixed delay", infoInitialDelay, infoFixedDelay);
    }

    // default 10 seconds delay and 5 seconds initial
    @Scheduled(initialDelayString = "${pds.config.trigger.nextjob.initialdelay:" + DEFAULT_INITIAL_DELAY_MILLIS
            + "}", fixedDelayString = "${pds.config.trigger.nextjob.delay:" + DEFAULT_FIXED_DELAY_MILLIS + "}")
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
        /*
         * find next job and mark it as already queued - so no other PDS instance does
         * try to process it
         */
        UUID uuid = null;

        boolean fetchedNextJob = false;
        while (!fetchedNextJob) {

            try {
                uuid = jobTransactionService.findNextJobToExecuteAndMarkAsQueued();

                fetchedNextJob = true;

            } catch (ObjectOptimisticLockingFailureException e) {
                /*
                 * This can happen when PDS instances are started at same time, so the check for
                 * next jobs can lead to race condiitons - and optmistic locks will occurre
                 * here.
                 *
                 * To avoid this to happen again, we wait a random time here. So next call on
                 * this machine should normally not collide again.
                 */
                Random random = new Random();
                int millis = random.ints(50, 3000).findFirst().getAsInt();
                LOG.info("Next job was already handled by another cluster member - will wait {} milliseconds and retry.", millis);
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
                LOG.info("Wait done - try again");
            }
        }
        if (uuid == null) {
            return;
        }
        executionService.addToExecutionQueueAsynchron(uuid);

    }
}