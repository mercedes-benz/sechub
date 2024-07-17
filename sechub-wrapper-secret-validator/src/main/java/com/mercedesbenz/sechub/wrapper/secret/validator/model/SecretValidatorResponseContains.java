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

    /**
     * Every time this setter is called the list will be cleared, but it can never
     * be null. In case the parameter is null the list, the list will stay empty,
     * but never null.
     *
     * @param allOf
     */
    public void setAllOf(List<String> allOf) {
        this.allOf.clear();
        if (allOf == null) {
            return;
        }
        this.allOf.addAll(allOf);
    }

    public List<String> getOneOf() {
        return Collections.unmodifiableList(oneOf);
    }

    /**
     * Every time this setter is called the list will be cleared, but it can never
     * be null. In case the parameter is null the list, the list will stay empty,
     * but never null.
     *
     * @param oneOf
     */
    public void setOneOf(List<String> oneOf) {
        this.oneOf.clear();
        if (oneOf == null) {
            return;
        }
        this.oneOf.addAll(oneOf);
    }

}
