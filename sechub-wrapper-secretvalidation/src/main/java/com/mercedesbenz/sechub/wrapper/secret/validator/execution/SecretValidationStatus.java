// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

public enum SecretValidationStatus {

    VALID("The secret was successfully validated via a configured URL!"),

    INVALID("The secret could not be validated via any configured URL!"),

    NO_VALIDATION_CONFIGURED("No validation URLs are configured for this type of secret!"),

    ALL_VALIDATION_REQUESTS_FAILED("All request to validate a secret failed, due to network or connection errors!"),

    SARIF_SNIPPET_NOT_SET("SARIF finding does not contain a valid snippet to validate!"),;

    private String description;

    private SecretValidationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
