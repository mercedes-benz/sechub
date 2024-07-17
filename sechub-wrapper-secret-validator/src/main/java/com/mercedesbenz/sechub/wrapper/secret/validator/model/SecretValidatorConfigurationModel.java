// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecretValidatorConfigurationModel {

    private String ruleId;
    private SecretValidatorCategorization categorization;
    private List<SecretValidatorRequest> requests = new ArrayList<>();

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public SecretValidatorCategorization getCategorization() {
        return categorization;
    }

    public void setCategorization(SecretValidatorCategorization categorization) {
        this.categorization = categorization;
    }

    public List<SecretValidatorRequest> getRequests() {
        return Collections.unmodifiableList(requests);
    }

    /**
     * Every time this setter is called the list will be cleared, but it can never
     * be null. In case the parameter is null the list, the list will stay empty,
     * but never null.
     *
     * @param requests
     */
    public void setRequests(List<SecretValidatorRequest> requests) {
        this.requests.clear();
        if (requests == null) {
            return;
        }
        this.requests.addAll(requests);
    }
}
