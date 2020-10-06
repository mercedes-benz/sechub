package com.daimler.sechub.domain.scan.product.config;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.error.NotFoundException;
import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorFetchesExecutorConfiguration;

@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
@Service
public class FetchProductExecutionProfileService {

    @Autowired
    ProductExecutionProfileRepository repository;

    @Autowired
    AuditLogService auditLogService;

    /* @formatter:off */
    @UseCaseAdministratorFetchesExecutorConfiguration(
            @Step(number = 1, 
            name = "Service call", 
            description = "Service reads setup information for an existing product executor configuration"))
    /* @formatter:on */
    public ProductExecutionProfile fetchProductExecutorConfig(String profileId) {
        auditLogService.log("Reads setup for executor configuration:{}", profileId);
        
        Optional<ProductExecutionProfile> config = repository.findById(profileId);
        if (! config.isPresent()) {
            throw new NotFoundException("Product execution profile not found for profileId:"+profileId);
        }
        
        return config.get();
    }

}
