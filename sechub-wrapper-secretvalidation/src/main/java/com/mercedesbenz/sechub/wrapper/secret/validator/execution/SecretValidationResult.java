// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

public class SecretValidationResult {

    private SecretValidationStatus validationStatus = SecretValidationStatus.NO_VALIDATION_CONFIGURED;
    private String validatedByUrl;

    /**
     * Get the status after validation of the current validated finding
     *
     * @return the validation status, never <code>null</code>
     */
    public SecretValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(SecretValidationStatus validationStatus) {
        if (validationStatus != null) {
            this.validationStatus = validationStatus;
        }
    }

    public String getValidatedByUrl() {
        return validatedByUrl;
    }

    public void setValidatedByUrl(String validatedByUrl) {
        this.validatedByUrl = validatedByUrl;
    }
}
