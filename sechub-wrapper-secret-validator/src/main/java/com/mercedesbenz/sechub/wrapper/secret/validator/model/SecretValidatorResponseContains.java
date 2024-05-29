// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecretValidatorResponseContains {

    private List<String> allOf = new ArrayList<>();
    private List<String> oneOf = new ArrayList<>();

    public List<String> getAllOf() {
        return Collections.unmodifiableList(allOf);
    }

    public void setAllOf(List<String> allOf) {
        if (allOf != null) {
            this.allOf = allOf;
        }
    }

    public List<String> getOneOf() {
        return Collections.unmodifiableList(oneOf);
    }

    public void setOneOf(List<String> oneOf) {
        if (oneOf != null) {
            this.oneOf = oneOf;
        }
    }

}
