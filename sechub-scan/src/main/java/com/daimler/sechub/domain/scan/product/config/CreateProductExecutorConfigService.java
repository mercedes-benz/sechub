// SPDX-License-Identifier: MIT
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
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorCreatesExecutorConfiguration;

@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
@Service
public class CreateProductExecutorConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(CreateProductExecutorConfigService.class);

    @Autowired
    ProductExecutorConfigRepository repository;

    @Autowired
    ProductExecutorConfigValidation validation;

    @Autowired
    AuditLogService auditLogService;

    /* @formatter:off */
    @UseCaseAdministratorCreatesExecutorConfiguration(
            @Step(number = 2, 
            name = "Service call", 
            description = "Service creates a new product executor configuration"))
    /* @formatter:on */
    public String createProductExecutorConfig(ProductExecutorConfig configFromUser) {
        assertValid(configFromUser, validation);

        auditLogService.log("Wants to create product execution configuration '{}', enabled: {} for executor:{}, V{}", configFromUser.getName(),
                configFromUser.getEnabled(), configFromUser.getProductIdentifier(), configFromUser.getExecutorVersion());

        resetFieldsNeverFromUser(configFromUser);


        ProductExecutorConfig stored = repository.save(configFromUser);
        UUID uuid = stored.getUUID();

        LOG.info("Created product execution configuration '{}', enabled={}, with uuidD={} for executor:{}, V{}", stored.getName(), configFromUser.getEnabled(),
                uuid, stored.getProductIdentifier(), stored.getExecutorVersion());

        return uuid.toString();

    }

    private void resetFieldsNeverFromUser(ProductExecutorConfig configFromUser) {
        configFromUser.uUID = null;
        configFromUser.version = null;
    }

}
