package com.mercedesbenz.sechub.domain.schedule;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.other.UseCaseSystemHandlesSigterm;

import jakarta.annotation.PreDestroy;

@Service
public class SchedulerTerminationService {

    @Autowired
    ScheduleJobMarkerService markerService;
    
    private boolean terminating;
    
    @PreDestroy
    @UseCaseSystemHandlesSigterm(@Step(number = 1, name = "Scheduler terminates", description = "Scheduler instance is termining. Will stop processing new jobs and mark current running jobs as PAUSED"))
    public void terminate() {
        terminating = true;
        /* FIXME Albert Tregnaghi, 2024-09-11: implement further! */
        // collect all current running sechub jobs inside this JVM
        List<UUID> sechubJobsOnThisMachine;

        // change all of these uuids to to state PAUSED
        // FIXME implement
//        markerService.markJobExecutionsPaused(sechubJobsOnThisMachine);
        
    }
    
    public boolean isTerminating() {
        return terminating;
    }
    // FIXME : be aware of race conditions with trigger service! */
    /* FIXME : shutdown hook integrate... and update field) */
}
