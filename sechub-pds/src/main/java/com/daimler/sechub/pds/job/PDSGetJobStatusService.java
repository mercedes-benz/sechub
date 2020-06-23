package com.daimler.sechub.pds.job;

import static com.daimler.sechub.pds.util.PDSAssert.*;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PDSGetJobStatusService {

    @Autowired
    PDSJobRepository repository;

    public PDSJobStatus getJobStatus(UUID jobUUID) {
        notNull(jobUUID, "job uuid may not be null!");
        
        PDSJob job = assertJobFound(jobUUID);
        
        return new PDSJobStatus(job);
    }
    
    private PDSJob assertJobFound(UUID jobUUID) {
        Optional<PDSJob> found = repository.findById(jobUUID);
        if (!found.isPresent()) {
            throw new IllegalArgumentException("Given job does not exist!");
        }
        PDSJob pdsJob = found.get();
        return pdsJob;
    }

}
