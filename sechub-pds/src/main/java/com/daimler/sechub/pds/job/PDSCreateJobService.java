package com.daimler.sechub.pds.job;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.pds.PDSJSONConverterException;
import com.daimler.sechub.pds.PDSNotAcceptableException;
import com.daimler.sechub.pds.config.PDSServerConfigurationService;
import com.daimler.sechub.pds.security.PDSUserContextService;
import com.daimler.sechub.pds.usecase.UseCaseUserCreatesJob;

@Service
public class PDSCreateJobService {

    @Autowired
    PDSUserContextService userContextService;

    @Autowired
    PDSJobRepository repository;

    @Autowired
    PDSJobConfigurationValidator configurationValidator;
    
    @Autowired
    PDSServerConfigurationService serverConfigurationService;

    @UseCaseUserCreatesJob
    public PDSJobCreateResult createJob(PDSJobConfiguration configuration) {
        
        configurationValidator.assertPDSConfigurationValid(configuration);

        PDSJob job = new PDSJob();

        job.created = LocalDateTime.now();
        job.state = PDSJobStatusState.CREATED;
        job.owner = userContextService.getUserId();
        job.setServerId(serverConfigurationService.getServerId());
        
        try {
            job.jsonConfiguration=configuration.toJSON();
        } catch (PDSJSONConverterException e) {
            throw new PDSNotAcceptableException("Configuration conversion failure:"+e.getMessage());
        }
        job = repository.save(job);

        return new PDSJobCreateResult(job.getUUID());
    }

}
