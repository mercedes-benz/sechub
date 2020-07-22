package com.daimler.sechub.domain.scan.product.config;

import static com.daimler.sechub.sharedkernel.validation.AssertValidation.*;

import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorUpdatesExecutorConfigSetup;

@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
@Service
public class UpdateProductExecutorConfigSetupService {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateProductExecutorConfigSetupService.class);

    @Autowired
    ProductExecutorConfigRepository repository;

    @Autowired
    ProductExecutorConfigSetupValidation validation;
    
    @Autowired
    AuditLogService auditLogService;

    /* @formatter:off */
    @UseCaseAdministratorUpdatesExecutorConfigSetup(
            @Step(number = 1, 
            name = "Service call", 
            description = "Service updates setup of an existing executor configuration"))
    /* @formatter:on */
    public UUID updateProductExecutorSetup(UUID uuid, ProductExecutorConfigSetup setup) {
        auditLogService.log("Wants to update product execution configuration setup for executor:{}", uuid);
    
        assertValid(setup, validation);
        
        ProductExecutorConfig config = repository.getOne(uuid);
        config.setSetup(setup.toJSON());
        
        ProductExecutorConfig stored = repository.save(config);

        LOG.info("Updated product execution configuration setup {} for executor:{}, V{}", uuid, stored.getProductIdentifier(), stored.getExecutorVersion());

        return uuid;

    }

}
