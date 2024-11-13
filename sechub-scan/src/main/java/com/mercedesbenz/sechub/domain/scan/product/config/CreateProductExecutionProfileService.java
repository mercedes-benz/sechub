// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.config;

import static com.mercedesbenz.sechub.sharedkernel.validation.AssertValidation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.AlreadyExistsException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminCreatesExecutionProfile;

import jakarta.annotation.security.RolesAllowed;

@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
@Service
public class CreateProductExecutionProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(CreateProductExecutionProfileService.class);

    @Autowired
    ProductExecutionProfileRepository repository;

    @Autowired
    ProductExecutorConfigRepository configRepository;

    @Autowired
    ProductExecutionProfileValidation validation;

    @Autowired
    AuditLogService auditLogService;

    /* @formatter:off */
    @UseCaseAdminCreatesExecutionProfile(
            @Step(number = 2,
            name = "Service call",
            description = "Service creates a new product executor configuration"))
    /* @formatter:on */
    public void createProductExecutionProfile(String profileId, ProductExecutionProfile profileFromUser) {
        profileFromUser.id = profileId;
        assertValid(profileFromUser, validation);

        auditLogService.log("Wants to create product execution profile'{}'", profileId);

        if (repository.existsById(profileId)) {
            throw new AlreadyExistsException("Profile already exists!");
        }
        resetFieldsNeverFromUser(profileFromUser);
        profileFromUser.id = profileId;

        List<ProductExecutorConfig> existingConfigurations = fetchExistingConfigurations(profileId, profileFromUser);

        profileFromUser.configurations.clear();
        profileFromUser.configurations.addAll(existingConfigurations);

        ProductExecutionProfile stored = repository.save(profileFromUser);

        LOG.info("Created product execution profile'{}' ", stored.getId());

    }

    private List<ProductExecutorConfig> fetchExistingConfigurations(String profileId, ProductExecutionProfile profileFromUser) {
        List<ProductExecutorConfig> list = new ArrayList<>();
        for (ProductExecutorConfig configFromUser : profileFromUser.configurations) {
            UUID uuid = configFromUser.getUUID();
            if (uuid == null) {
                LOG.warn("config uuid null not accepted - so ignoring for profile {}", profileId);
                continue;
            }
            Optional<ProductExecutorConfig> opt = configRepository.findById(uuid);
            if (!opt.isPresent()) {
                LOG.warn("config with uuid {} not found -  so ignoring for profile {}", uuid, profileId);
                continue;
            }
            list.add(opt.get());
        }
        return list;
    }

    private void resetFieldsNeverFromUser(ProductExecutionProfile configFromUser) {
        configFromUser.version = null;
    }

}
