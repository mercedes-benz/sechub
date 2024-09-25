// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecretValidatorCategorization {

    private String defaultSeverity;
    private String validationFailedSeverity;
    private String validationSuccessSeverity;

    public String getDefaultSeverity() {
        return defaultSeverity;
    }

    public void setDefaultSeverity(String defaultSeverity) {
        this.defaultSeverity = defaultSeverity;
    }

    public String getValidationFailedSeverity() {
        return validationFailedSeverity;
    }

    public void setValidationFailedSeverity(String validationFailedSeverity) {
        this.validationFailedSeverity = validationFailedSeverity;
    }

    public String getValidationSuccessSeverity() {
        return validationSuccessSeverity;
    }

    public void setValidationSuccessSeverity(String validationSuccessSeverity) {
        this.validationSuccessSeverity = validationSuccessSeverity;
    }

    /**
     * Check if no severities were configured for.
     *
     * @return true, if all fields are <code>null</code> and therefore not
     *         configured and false, if any of the fields is set to a value other
     *         than <code>null</code>.
     */
    @JsonIgnore
    public boolean isEmpty() {
        return defaultSeverity == null && validationFailedSeverity == null && validationSuccessSeverity == null;
    }

}
