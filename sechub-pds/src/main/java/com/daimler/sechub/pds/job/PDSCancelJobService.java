package com.daimler.sechub.pds.job;

import static com.daimler.sechub.pds.job.PDSJobAssert.*;
import static com.daimler.sechub.pds.util.PDSAssert.*;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.pds.execution.PDSExecutionService;
@Service
public class PDSCancelJobService {

    @Autowired
    PDSJobRepository repository;

    @Autowired
    PDSExecutionService executionService;

    public void cancelJob(UUID jobUUID) {
        notNull(jobUUID, "job uuid may not be null!");

        PDSJob job = assertJobFound(jobUUID,repository);
        assertJobIsInState(job,PDSJobStatusState.RUNNING);

        executionService.cancel(jobUUID);

        job.setState(PDSJobStatusState.CANCELED);

        repository.save(job);

    }

}
