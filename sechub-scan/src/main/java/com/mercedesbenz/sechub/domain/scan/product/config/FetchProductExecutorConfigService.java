// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.config;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesExecutorConfiguration;

import jakarta.annotation.security.RolesAllowed;

@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
@Service
public class FetchProductExecutorConfigService {

    @Autowired
    ProductExecutorConfigRepository repository;

    @Autowired
    AuditLogService auditLogService;

    /* @formatter:off */
    @UseCaseAdminFetchesExecutorConfiguration(
            @Step(number = 2,
            name = "Service call",
            description = "Service reads setup information for an existing product executor configuration"))
    /* @formatter:on */
    public ProductExecutorConfig fetchProductExecutorConfig(UUID uuid) {
        auditLogService.log("Reads setup for executor configuration:{}", uuid);

        Optional<ProductExecutorConfig> config = repository.findById(uuid);
        if (config.isEmpty()) {
            throw new NotFoundException("Product executor config not found for uuid:" + uuid);
        }

        return config.get();
    }

}
