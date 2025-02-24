// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SecHubConfigurationModelValidationResult {

    private List<SecHubConfigurationModelValidationErrorData> errors = new ArrayList<>(1);

    public boolean hasError(SecHubConfigurationModelValidationError error) {
        return findFirstOccurrenceOf(error) != null;
    }

    public List<SecHubConfigurationModelValidationErrorData> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Tries to find error data for first occurrence of given error inside this
     * result
     *
     * @param error
     * @return error data or <code>null</code> if such error is not inside this
     *         result
     */
    public SecHubConfigurationModelValidationErrorData findFirstOccurrenceOf(SecHubConfigurationModelValidationError error) {
        for (SecHubConfigurationModelValidationErrorData data : errors) {
            if (data.error == error) {
                return data;
            }
        }
        return null;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    void addError(SecHubConfigurationModelValidationError error) {
        addError(error, null);
    }

    void addError(SecHubConfigurationModelValidationError error, String additionalInfo) {
        if (error == null) {
            throw new IllegalArgumentException("Error object may not be null!");
        }
        SecHubConfigurationModelValidationErrorData data = new SecHubConfigurationModelValidationErrorData();
        data.error = error;
        data.message = error.getDefaultMessage();
        if (additionalInfo != null) {
            data.message = data.message + " " + additionalInfo;
        }
        errors.add(data);
    }

    public class SecHubConfigurationModelValidationErrorData {

        private SecHubConfigurationModelValidationError error;
        private String message;

        public SecHubConfigurationModelValidationError getError() {
            return error;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "SecHubConfigurationModelValidationErrorData [" + (error != null ? "error=" + error + ", " : "")
                    + (message != null ? "message=" + message : "") + "]";
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SecHubConfigurationModelValidationResult:\n");

        errors.forEach((error) -> sb.append("- " + error.toString() + "\n"));
        return sb.toString();
    }

}