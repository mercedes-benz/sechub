// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.validation;

import org.springframework.stereotype.Component;

@Component
public class ApiVersionValidationFactory {

    /**
     * Creates a new version validation instance accepting only given versions. 
     * @param versions
     * @return
     */
    public ApiVersionValidation createValidationAccepting(String ...acceptedVersions) {
        return new ApiVersionValidationImpl(acceptedVersions);
    }
}
