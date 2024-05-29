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
        // if no expected HTTP status code was configured inside the config we ignore it
        if (expectedHttpStatus > 0) {
            // no contains section specified, we return the result of the checked status
            // code
            if (hasNoContainsSection(secretValidatorResponse.getContains())) {
                return expectedHttpStatus == response.statusCode();
            }
            return containsExpectedSnippets(secretValidatorResponse.getContains(), response.body());
        } else {
            // no contains section specified, we return false, since nothing was specified
            // at all to check the response
            if (hasNoContainsSection(secretValidatorResponse.getContains())) {
                return false;
            }
            return containsExpectedSnippets(secretValidatorResponse.getContains(), response.body());
        }
    }

    private boolean hasNoContainsSection(SecretValidatorResponseContains contains) {
        if (contains == null) {
            return true;
        }
        return contains.getAllOf().isEmpty() && contains.getOneOf().isEmpty();
    }

    private boolean containsExpectedSnippets(SecretValidatorResponseContains contains, String body) {
        // body should not be null since contains section should be specified at this
        // point
        if (body == null || contains == null) {
            return false;
        }

        // at the point where this method gets called we require the contains section to
        // be configured
        if (hasNoContainsSection(contains)) {
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
