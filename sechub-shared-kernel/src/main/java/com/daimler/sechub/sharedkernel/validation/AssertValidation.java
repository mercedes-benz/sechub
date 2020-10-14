// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.sharedkernel.error.NotAcceptableException;

public class AssertValidation {
    
    private static final Logger LOG = LoggerFactory.getLogger(AssertValidation.class);

    
    /**
     * Asserts target is valid. Otherwise a {@link NotAcceptableException} will be
     * thrown with result description as message
     * 
     * @param target
     */
    public static <T> void assertValid(T target, Validation<T> validation) {
        assertValid(target, validation,null);
    }
    
    /**
     * Asserts target is valid. Otherwise a {@link NotAcceptableException} will be
     * thrown with given error message in combination with result description
     * 
     * @param target
     * @param errorMessage
     */
    public static <T> void assertValid(T target, Validation<T> validation, String errorMessage) {
        ValidationResult result = validation.validate(target);
        if (result.isValid()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (errorMessage!=null) {
            sb.append(errorMessage).append(". ");
        }
        sb.append(result.getErrorDescription());
        LOG.error("Validation failed , result was {}", result.getErrorDescription());
        throw new NotAcceptableException(sb.toString());
    }

}
