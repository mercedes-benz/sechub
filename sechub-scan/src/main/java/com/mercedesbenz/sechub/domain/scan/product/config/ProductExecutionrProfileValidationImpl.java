// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.validation.AbstractValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ProductExecutionProfileIdValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ProfileDescriptionValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ProjectIdValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ValidationContext;

@Component
public class ProductExecutionrProfileValidationImpl extends AbstractValidation<ProductExecutionProfile> implements ProductExecutionProfileValidation {

    @Autowired
    ProductExecutionProfileIdValidation profileIdValidation;

    @Autowired
    ProjectIdValidation projectIdValidation;

    @Autowired
    ProfileDescriptionValidation descriptionValidation;

    protected String getValidatorName() {
        return "product execution profile validation";
    }

    @Override
    protected void setup(AbstractValidation<ProductExecutionProfile>.ValidationConfig config) {
    }

    @Override
    protected void validate(ValidationContext<ProductExecutionProfile> context) {
        validateNotNull(context);

        ProductExecutionProfile profile = getObjectToValidate(context);
        if (profile == null) {
            return;
        }
        context.addErrors(profileIdValidation.validate(profile.getId()));
        context.addErrors(descriptionValidation.validate(profile.getDescription()));

        for (String projectId : profile.getProjectIds()) {
            context.addErrors(projectIdValidation.validate(projectId));
        }

        /* check we got at last a UUID inside each configuration */
        for (ProductExecutorConfig config : profile.getConfigurations()) {
            if (config.getUUID() == null) {
                context.addError("config.uuid", "One configuration does not have UUID key set!");
                break;
            }
        }

    }

}
