// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

import java.net.http.HttpResponse;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorResponse;
import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorResponseContains;

@Service
public class ResponseValidationService {

    public boolean isValidResponse(HttpResponse<String> response, SecretValidatorResponse secretValidatorResponse) {
        if (response == null || secretValidatorResponse == null) {
            return false;
        }
        int expectedHttpStatus = secretValidatorResponse.getHttpStatus();
        SecretValidatorResponseContains containsSection = secretValidatorResponse.getContains();

        // if the expected status is not set and no contains section is set, the secret
        // cannot be validated
        if (expectedHttpStatus <= 0 && hasNoContainsSection(containsSection)) {
            return false;
        }

        // no contains section set, so we use the status code
        if (hasNoContainsSection(containsSection)) {
            return expectedHttpStatus == response.statusCode();
        }

        // contains section and expected status code set, both must be valid
        if (expectedHttpStatus > 0) {
            boolean valid = expectedHttpStatus == response.statusCode();
            return valid && containsExpectedSnippets(containsSection, response.body());
        }

        // fallback if only a contains section was configured
        return containsExpectedSnippets(containsSection, response.body());
    }

    private boolean hasNoContainsSection(SecretValidatorResponseContains contains) {
        if (contains == null) {
            return true;
        }
        return contains.getAllOf().isEmpty() && contains.getOneOf().isEmpty();
    }

    private boolean containsExpectedSnippets(SecretValidatorResponseContains contains, String body) {
        // body and contains section should be specified at this point
        if (body == null || contains == null) {
            return false;
        }

        for (String substring : contains.getAllOf()) {
            // all must be present
            if (!body.contains(substring)) {
                return false;
            }
        }
        if (contains.getOneOf().isEmpty()) {
            return true;
        }

        for (String substring : contains.getOneOf()) {
            // if one is present it is enough at this point
            if (body.contains(substring)) {
                return true;
            }
        }
        // this should not be reached
        return false;
    }

}
