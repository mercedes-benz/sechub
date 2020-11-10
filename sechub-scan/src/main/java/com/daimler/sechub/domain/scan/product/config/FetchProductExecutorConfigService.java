// SPDX-License-Identifier: MIT
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
public class FetchProductExecutorConfigService {

    @Autowired
    ProductExecutorConfigRepository repository;

    @Autowired
    AuditLogService auditLogService;

    /* @formatter:off */
    @UseCaseAdministratorFetchesExecutorConfiguration(
            @Step(number = 2, 
            name = "Service call", 
            description = "Service reads setup information for an existing product executor configuration"))
    /* @formatter:on */
    public ProductExecutorConfig fetchProductExecutorConfig(UUID uuid) {
        auditLogService.log("Reads setup for executor configuration:{}", uuid);
        
        Optional<ProductExecutorConfig> config = repository.findById(uuid);
        if (! config.isPresent()) {
            throw new NotFoundException("Product executor config not found for uuid:"+uuid);
        }
        
        return config.get();
    }

}
