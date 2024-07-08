// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.config;

import static com.mercedesbenz.sechub.sharedkernel.validation.AssertValidation.assertValid;

import java.util.Optional;
import java.util.Set;
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
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminAssignsExecutionProfileToProject;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminUnassignsExecutionProfileFromProject;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminUpdatesExecutorConfig;
import com.mercedesbenz.sechub.sharedkernel.validation.ProductExecutionProfileIdValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ProjectIdValidation;

import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;

@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
@Service
public class UpdateProductExecutionProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateProductExecutionProfileService.class);

    @Autowired
    ProductExecutionProfileRepository repository;

    @Autowired
    ProductExecutorConfigRepository configRepository;

    @Autowired
    ProductExecutionProfileValidation profileValidation;

    @Autowired
    ProductExecutionProfileIdValidation profileIdValidation;

    @Autowired
    ProjectIdValidation projectIdValidation;

    @Autowired
    AuditLogService auditLogService;

    /* @formatter:off */
    @UseCaseAdminUpdatesExecutorConfig(
            @Step(number = 2,
            name = "Service call",
            description = "Service updates existing executor configuration"))
    /* @formatter:on */
    public void updateExecutionProfile(String profileId, ProductExecutionProfile profileFromUser) {
        profileFromUser.id = profileId;
        assertValid(profileFromUser, profileValidation);

        auditLogService.log("Wants to update product execution configuration setup for executor:{}", profileId);

        Optional<ProductExecutionProfile> opt = repository.findById(profileId);
        if (opt.isEmpty()) {
            throw new NotFoundException("No profile found with id:" + profileId);
        }
        profileFromUser.id = profileId;

        ProductExecutionProfile stored = mergeFromUserProfileIntoEntity(profileId, profileFromUser, opt);

        repository.save(stored);

        LOG.info("Updated product execution profile {}", profileId);

    }

    private ProductExecutionProfile mergeFromUserProfileIntoEntity(String profileId, ProductExecutionProfile profileFromUser,
            Optional<ProductExecutionProfile> opt) {

        ProductExecutionProfile stored = opt.get();
        stored.description = profileFromUser.description;
        stored.enabled = profileFromUser.enabled;

        /* we change no profile associations with project ids */

        /* update configurations by given ids */
        stored.configurations.clear();

        Set<ProductExecutorConfig> configurationsFromUser = profileFromUser.getConfigurations();
        for (ProductExecutorConfig configFromUser : configurationsFromUser) {
            UUID uuid = configFromUser.getUUID();
            Optional<ProductExecutorConfig> found = configRepository.findById(uuid);
            if (found.isEmpty()) {
                LOG.warn("Found no configuration with uuid:{}, so cannot add to profile:{}", uuid, profileId);
                continue;
            }
            stored.configurations.add(found.get());
        }
        return stored;
    }

    @Transactional
    @UseCaseAdminAssignsExecutionProfileToProject(@Step(number = 2, name = "Service call", description = "Services creates a new association between project id and profile"))
    public void addProjectToProfileRelation(String profileId, String projectId) {
        assertValid(profileId, profileIdValidation);
        assertValid(projectId, projectIdValidation);

        auditLogService.log("Wants to add association between project {} and profile {}", projectId, profileId);

        int count = repository.countRelationShipEntries(profileId, projectId);
        if (count != 0) {
            LOG.debug("project {} is already added to profile {} - so just skip", projectId, profileId);
            return;
        }
        repository.createProfileRelationToProject(profileId, projectId);
        LOG.info("project {} added to profile {}", projectId, profileId);
    }

    @Transactional
    @UseCaseAdminUnassignsExecutionProfileFromProject(@Step(number = 2, name = "Service call", description = "Services deletes an existing association between project id and profile"))
    public void removeProjectToProfileRelation(String profileId, String projectId) {
        assertValid(profileId, profileIdValidation);
        assertValid(projectId, projectIdValidation);

        repository.deleteProfileRelationToProject(profileId, projectId);
    }

}
