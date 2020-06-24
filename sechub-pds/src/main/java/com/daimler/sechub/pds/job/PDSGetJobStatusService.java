package com.daimler.sechub.pds.job;

import static com.daimler.sechub.pds.job.PDSJobAssert.*;
import static com.daimler.sechub.pds.util.PDSAssert.*;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PDSGetJobStatusService {

    @Autowired
    PDSJobRepository repository;

    public PDSJobStatus getJobStatus(UUID jobUUID) {
        notNull(jobUUID, "job uuid may not be null!");
        
        PDSJob job = assertJobFound(jobUUID,repository);
        
        return new PDSJobStatus(job);
    }
    

}
