package com.daimler.sechub.pds.job;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PDSCreateJobService {

    @Autowired
    PDSJobRepository repository;
    
    public PDSJobCreateResult createJob(PDSConfiguration configuration) {
        PDSJob job = new PDSJob();
        
        job.sechubJobUUID=configuration.getSechubJobUUID();
        job.created=LocalDateTime.now();
        job.state=PDSJobStatusState.CREATED;
        
        job = repository.save(job);
        
        return new PDSJobCreateResult(job.getUUID());
    }

}
