package com.daimler.sechub.pds.job;

import static com.daimler.sechub.pds.util.PDSAssert.*;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PDSGetJobResultService {
    
    @Autowired
    PDSJobRepository repository;

    public String getJobResult(UUID jobUUID) {
        notNull(jobUUID, "job uuid may not be null!");
        
        PDSJob job = assertJobFound(jobUUID);
        assertJobIsInStateDone(job);
        return job.getResult();
    }
    
    private PDSJob assertJobFound(UUID jobUUID) {
        Optional<PDSJob> found = repository.findById(jobUUID);
        if (!found.isPresent()) {
            throw new IllegalArgumentException("Given job does not exist!");
        }
        PDSJob pdsJob = found.get();
        return pdsJob;
    }

    private void assertJobIsInStateDone(PDSJob job) {
        PDSJobStatusState jobState = job.getState();
        if (!jobState.equals(PDSJobStatusState.DONE)) {
            throw new IllegalStateException("Cannot get job result, because job in state:" + jobState);
        }
    }

}
