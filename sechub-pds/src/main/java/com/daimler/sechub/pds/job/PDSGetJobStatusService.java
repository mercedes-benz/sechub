package com.daimler.sechub.pds.job;

import static com.daimler.sechub.pds.job.PDSJobAssert.*;
import static com.daimler.sechub.pds.util.PDSAssert.*;

import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.pds.security.PDSRoleConstants;
import com.daimler.sechub.pds.usecase.PDSStep;
import com.daimler.sechub.pds.usecase.UseCaseUserFetchesJobStatus;

@Service
@RolesAllowed({PDSRoleConstants.ROLE_SUPERADMIN, PDSRoleConstants.ROLE_USER})
public class PDSGetJobStatusService {

    @Autowired
    PDSJobRepository repository;

    @UseCaseUserFetchesJobStatus(@PDSStep(name="service call",description = "returns job status",number=2))
    public PDSJobStatus getJobStatus(UUID jobUUID) {
        notNull(jobUUID, "job uuid may not be null!");
        
        PDSJob job = assertJobFound(jobUUID,repository);
        
        return new PDSJobStatus(job);
    }
    

}
