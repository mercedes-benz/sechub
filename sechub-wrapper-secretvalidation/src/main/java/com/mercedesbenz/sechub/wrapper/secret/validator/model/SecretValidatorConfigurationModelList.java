// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecretValidatorConfigurationModelList {

    private List<SecretValidatorConfigurationModel> validatorConfigList = new ArrayList<>();

    public List<SecretValidatorConfigurationModel> getValidatorConfigList() {
        return Collections.unmodifiableList(validatorConfigList);
    }

    /**
     * Every time this setter is called the list will be cleared, but it can never
     * be null. In case the parameter is null the list, the list will stay empty,
     * but never null.
     *
     * @param validatorConfigList
     */
    public void setValidatorConfigList(List<SecretValidatorConfigurationModel> validatorConfigList) {
        this.validatorConfigList.clear();
        if (validatorConfigList == null) {
            return;
        }
        this.validatorConfigList.addAll(validatorConfigList);
    }

}
