package com.daimler.sechub.pds.job;

import static com.daimler.sechub.pds.util.PDSAssert.*;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PDSMarkReadyToStartJobService {

    @Autowired
    PDSJobRepository repository;

    public void markReadyToStart(UUID jobUUID) {
        notNull(jobUUID, "job uuid may not be null!");
        
        Optional<PDSJob> found = repository.findById(jobUUID);
        if (! found.isPresent()) {
            throw new IllegalArgumentException("Given job does not exist!");
        }
        PDSJob pdsJob = found.get();
        pdsJob.setState(PDSJobStatusState.READY_TO_START);
        
        repository.save(pdsJob);
    }

}
