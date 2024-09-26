package com.mercedesbenz.sechub.domain.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.schedule.batch.SynchronSecHubJobExecutor;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.other.UseCaseSystemSuspendsJobsWhenSigTermReceived;

import jakarta.annotation.PreDestroy;

@Service
public class SchedulerTerminationService {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerTerminationService.class);

    @Autowired
    ScheduleJobMarkerService markerService;

    @Autowired
    SynchronSecHubJobExecutor executor;

    private boolean terminating;

    @PreDestroy
    @UseCaseSystemSuspendsJobsWhenSigTermReceived(@Step(number = 1, name = "Scheduler terminates", description = "Scheduler instance is terminating. Will stop processing new jobs and inform job executor to suspend"))
    public void terminate() {

        LOG.info("Start termination process");
        if (terminating) {
            LOG.info("Alrady in termination process! Will skip request");
            return;
        }

        /* set flag for this service */
        terminating = true;

        /* stop execution of all jobs and suspend all running ones */
        executor.suspend();

    }

    /**
     * Only for integration testing!
     */
    void internalResetTermination() {
        LOG.warn("Reset termination process - may only happen inside tests!");
        executor.internalResetSuspensionState();

        this.terminating = false;
    }

    public boolean isTerminating() {
        return terminating;
    }
}
