// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import java.time.LocalDateTime;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.pds.PDSJSONConverterException;
import com.daimler.sechub.pds.PDSNotAcceptableException;
import com.daimler.sechub.pds.config.PDSServerConfigurationService;
import com.daimler.sechub.pds.security.PDSRoleConstants;
import com.daimler.sechub.pds.security.PDSUserContextService;
import com.daimler.sechub.pds.usecase.PDSStep;
import com.daimler.sechub.pds.usecase.UseCaseUserCreatesJob;

@Service
@RolesAllowed({PDSRoleConstants.ROLE_USER, PDSRoleConstants.ROLE_SUPERADMIN})
public class PDSCreateJobService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSCreateJobService.class);

    @Autowired
    PDSUserContextService userContextService;

    @Autowired
    PDSJobRepository repository;

    @Autowired
    PDSJobConfigurationValidator configurationValidator;
    
    @Autowired
    PDSServerConfigurationService serverConfigurationService;

    @UseCaseUserCreatesJob(@PDSStep(name="service call",description = "job will be created, serverId will be used to store new job",number=2))
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
        
        LOG.info("Job {} has been created", job.getUUID());

        return new PDSJobCreateResult(job.getUUID());
    }

}
