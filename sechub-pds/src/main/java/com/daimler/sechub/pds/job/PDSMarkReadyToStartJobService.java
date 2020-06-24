package com.daimler.sechub.pds.job;

import static com.daimler.sechub.pds.job.PDSJobAssert.*;
import static com.daimler.sechub.pds.util.PDSAssert.*;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PDSMarkReadyToStartJobService {

    @Autowired
    PDSJobRepository repository;

    public void markReadyToStart(UUID jobUUID) {
        notNull(jobUUID, "job uuid may not be null!");

        PDSJob job = assertJobFound(jobUUID,repository);
        assertJobIsInState(job,PDSJobStatusState.CREATED);

        job.setState(PDSJobStatusState.READY_TO_START);
        repository.save(job);
    }


}
