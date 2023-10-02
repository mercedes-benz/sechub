// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.config;

import static com.mercedesbenz.sechub.sharedkernel.validation.AssertValidation.*;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminUpdatesExecutorConfig;

import jakarta.annotation.security.RolesAllowed;

@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
@Service
public class UpdateProductExecutorConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateProductExecutorConfigService.class);

    @Autowired
    ProductExecutorConfigRepository repository;

    @Autowired
    ProductExecutorConfigValidation validation;

    @Autowired
    AuditLogService auditLogService;

    /* @formatter:off */
    @UseCaseAdminUpdatesExecutorConfig(
            @Step(number = 2,
            name = "Service call",
            description = "Service updates existing executor configuration"))
    /* @formatter:on */
    public UUID updateProductExecutorSetup(UUID uuid, ProductExecutorConfig configFromUser) {
        assertValid(configFromUser, validation);

        auditLogService.log("Wants to update product execution configuration setup for executor:{}", uuid);
        Optional<ProductExecutorConfig> opt = repository.findById(uuid);
        if (!opt.isPresent()) {
            throw new NotFoundException("No config found with uuid:" + uuid);
        }

        ProductExecutorConfig stored = opt.get();

        stored.name = configFromUser.getName();
        stored.executorVersion = configFromUser.getExecutorVersion();
        stored.productIdentifier = configFromUser.getProductIdentifier();
        stored.enabled = configFromUser.getEnabled();
        stored.setup = configFromUser.getSetup(); // full replacement of setup - is stored in DB as JSON string

        repository.save(stored);

        LOG.info("Updated product execution configuration setup {},name='{}', enabled={} for executor:{} V{}", uuid, stored.getName(), stored.getEnabled(),
                stored.getProductIdentifier(), stored.getExecutorVersion());

        return uuid;

    }

}
