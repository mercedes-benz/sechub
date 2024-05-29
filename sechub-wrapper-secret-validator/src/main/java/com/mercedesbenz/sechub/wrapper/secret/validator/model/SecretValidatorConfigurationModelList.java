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

    public void setDataList(List<SecretValidatorConfigurationModel> validatorConfigList) {
        if (validatorConfigList != null) {
            this.validatorConfigList = validatorConfigList;
        }
    }

}
