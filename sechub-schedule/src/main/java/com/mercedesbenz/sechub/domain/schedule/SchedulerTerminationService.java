package com.mercedesbenz.sechub.domain.schedule;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SchedulerTerminationService {

    @Autowired
    ScheduleJobMarkerService markerService;
    
    private boolean terminating;
    
    private void handleTermination() {
        
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
