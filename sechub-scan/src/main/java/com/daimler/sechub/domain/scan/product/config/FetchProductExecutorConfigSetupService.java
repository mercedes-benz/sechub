package com.daimler.sechub.domain.scan.product.config;

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
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorAddsExecutorConfiguration;

@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
@Service
public class FetchProductExecutorConfigSetupService {

    private static final Logger LOG = LoggerFactory.getLogger(FetchProductExecutorConfigSetupService.class);

    @Autowired
    ProductExecutorConfigRepository repository;

    @Autowired
    AuditLogService auditLogService;

    /* @formatter:off */
    @UseCaseAdministratorAddsExecutorConfiguration(
            @Step(number = 1, 
            name = "Service call", 
            description = "Service reads setup information for an existing product executor configuration"))
    /* @formatter:on */
    public ProductExecutorConfigSetup fetchProductExecutorConfigSetup(UUID uuid) {
        auditLogService.log("Reads setup for executor configuration:{}", uuid);
        ProductExecutorConfig setup = repository.getOne(uuid);
        
        LOG.debug("Found product execution configuration {} for executor:{}, V{}", uuid, setup.getProductIdentifier(), setup.getExecutorVersion());
        String setupJSON = setup.getSetup();
        return ProductExecutorConfigSetup.fromJSONString(setupJSON);

    }

}
