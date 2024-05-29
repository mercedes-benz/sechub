// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecretValidatorResponse {

    private int httpStatus;
    private SecretValidatorResponseContains contains;

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public SecretValidatorResponseContains getContains() {
        return contains;
    }

    public void setContains(SecretValidatorResponseContains contains) {
        this.contains = contains;
    }

}
