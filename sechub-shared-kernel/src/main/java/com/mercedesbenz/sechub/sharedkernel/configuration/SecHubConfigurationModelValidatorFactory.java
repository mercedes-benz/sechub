// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.configuration;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidator;

@Component
public class SecHubConfigurationModelValidatorFactory {

    public SecHubConfigurationModelValidator createValidator() {
        return new SecHubConfigurationModelValidator();
    }
}
