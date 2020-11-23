// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.validation;

import java.net.URI;

import org.springframework.stereotype.Component;

@Component
public class URIValidationImpl extends AbstractValidation<URI> implements URIValidation {

    public static final int URI_LENGTH_MAX = 255;
    
    @Override
    protected void setup(AbstractValidation<URI>.ValidationConfig config) {

    }

    @Override
    protected void validate(ValidationContext<URI> context) {
        validateNotNull(context);
        URI uri = context.objectToValidate;
        if (uri == null) {
            /* already handled before */
            return;
        }
        
        if (uri.toString().length() > URI_LENGTH_MAX) {
            addErrorMessage(context, "URI exceeds maximum of " + URI_LENGTH_MAX + " characters");
        }

        /* we accept no empty URIs - which is allowed in java" */
        String simpleString = uri.toString().trim();
        if (simpleString.isEmpty()) {
            addErrorMessage(context, "May not be empty!");
            return;
        }
    }

    @Override
    protected String getValidatorName() {
        return "URI validation";
    }

}
