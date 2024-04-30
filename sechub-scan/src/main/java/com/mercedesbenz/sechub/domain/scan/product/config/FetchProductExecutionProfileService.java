// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.config;

import static com.mercedesbenz.sechub.sharedkernel.validation.AssertValidation.*;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesExecutionProfile;
import com.mercedesbenz.sechub.sharedkernel.validation.ProductExecutionProfileIdValidation;

import jakarta.annotation.security.RolesAllowed;

@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
@Service
public class FetchProductExecutionProfileService {

    @Autowired
    ProductExecutionProfileRepository repository;

    @Autowired
    ProductExecutionProfileIdValidation profileIdValidation;

    @Autowired
    AuditLogService auditLogService;

    /* @formatter:off */
    @UseCaseAdminFetchesExecutionProfile(
            @Step(number = 2,
            name = "Service call",
            description = "Service reads setup information for an existing product executor configuration"))
    /* @formatter:on */
    public ProductExecutionProfile fetchProductExecutorConfig(String profileId) {
        assertValid(profileId, profileIdValidation);

        auditLogService.log("Reads setup for executor configuration:{}", profileId);

        Optional<ProductExecutionProfile> config = repository.findById(profileId);
        if (!config.isPresent()) {
            throw new NotFoundException("Product execution profile not found for profileId:" + profileId);
        }

        return config.get();
    }

}
