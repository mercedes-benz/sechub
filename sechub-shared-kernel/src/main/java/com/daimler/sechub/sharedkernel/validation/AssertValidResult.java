// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.validation;

import com.daimler.sechub.sharedkernel.error.NotAcceptableException;

public class AssertValidResult {
    /**
     * Asserts validation result valid
     *
     * @param errorMessage
     * @param target
     */
    public static <T> void assertValid(ValidationResult result) {
        assertValid(result, null);
    }

    /**
     * Asserts validation result valid
     *
     * @param errorMessage
     * @param target
     */
    public static <T> void assertValid(ValidationResult result, String errorMessage) {
        if (result == null) {
            throw new IllegalArgumentException("Result may not be null!");
        }
        if (result.isValid()) {
            return;
        }
        if (errorMessage != null) {
            throw new NotAcceptableException(errorMessage + ". " + result.getErrorDescription());
        }
        throw new NotAcceptableException(result.getErrorDescription());
    }
}
