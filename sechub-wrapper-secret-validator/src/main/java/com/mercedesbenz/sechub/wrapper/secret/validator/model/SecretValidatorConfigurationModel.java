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

    public void setRequests(List<SecretValidatorRequest> requests) {
        if (requests != null) {
            this.requests = requests;
        }
    }
}
