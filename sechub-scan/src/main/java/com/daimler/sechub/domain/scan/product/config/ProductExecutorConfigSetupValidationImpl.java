// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.validation.AbstractValidation;
import com.daimler.sechub.sharedkernel.validation.ValidationContext;

@Component
public class ProductExecutorConfigSetupValidationImpl extends AbstractValidation<ProductExecutorConfigSetup> implements ProductExecutorConfigSetupValidation{

    protected String getValidatorName() {
        return "product executor config setup validation";
    }

    @Override
    protected void setup(AbstractValidation<ProductExecutorConfigSetup>.ValidationConfig config) {
        
    }

    @Override
    protected void validate(ValidationContext<ProductExecutorConfigSetup> context) {
        validateNotNull(context);
        ProductExecutorConfigSetup config = getObjectToValidate(context);
        validateNotNull(context, config.getBaseURL(), "base url");
        validateNotNull(context, config.getJobParameters(), "job parameters");
        validateMaxSize(context, config.getJobParameters(), 100, "maximum");
        
        ProductExecutorConfigSetupCredentials credentials = config.getCredentials();
        validateNotNull(context, credentials, "credentials");
        if (credentials!=null) {
            String user = credentials.getUser();
            String password = credentials.getPassword();
            
            validateMaxLength(context, user, 40,"credentials.user");
            validateMaxLength(context, password, 40,"credentials.pwd");
        }
    }

}
