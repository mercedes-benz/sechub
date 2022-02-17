// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.config;

import static com.mercedesbenz.sechub.sharedkernel.validation.AssertValidation.*;

import java.util.Optional;

import javax.annotation.security.RolesAllowed;

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
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminDeletesExecutionProfile;
import com.mercedesbenz.sechub.sharedkernel.validation.ProductExecutionProfileIdValidation;

@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
@Service
public class DeleteProductExecutionProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteProductExecutionProfileService.class);

    @Autowired
    ProductExecutionProfileRepository repository;

    @Autowired
    ProductExecutionProfileIdValidation profileIdValidation;

    @Autowired
    AuditLogService auditLogService;

    /* @formatter:off */
    @UseCaseAdminDeletesExecutionProfile(
            @Step(
                number=2,
                name="Service call",
                description="Service deletes an existing product execution profile by its profile id"))
    public void deleteProductExecutionProfile(String profileId) {
        assertValid(profileId, profileIdValidation);

        auditLogService.log("Wants to removed product execution profile {}",profileId);

        Optional<ProductExecutionProfile> opt = repository.findById(profileId);
        if (!opt.isPresent()) {
            LOG.info("Delete canceled, because execution profile with id {} did not exist.",profileId);;
            throw new NotFoundException("Profile "+profileId+" does not exist, so cannot be deleted");
        }
        ProductExecutionProfile found = opt.get();
        String description = found.getDescription();

        LOG.debug("Start removing configurations and project ids from profile:{}",profileId);
        found.getConfigurations().clear();
        found.getProjectIds().clear();
        repository.saveAndFlush(found);

        LOG.debug("Start delete of profile:{}",profileId);
        repository.deleteById(profileId);

        LOG.info("Removed product execution profile id:{}, description:{}",profileId, description);
    }

    /* @formatter:on */

}
