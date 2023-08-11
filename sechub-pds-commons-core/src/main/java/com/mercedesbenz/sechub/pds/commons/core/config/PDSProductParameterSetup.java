// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.commons.core.config;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PDSProductParameterSetup {

    private List<PDSProductParameterDefinition> mandatory = new ArrayList<>();
    private List<PDSProductParameterDefinition> optional = new ArrayList<>();

    public List<PDSProductParameterDefinition> getMandatory() {
        return mandatory;
    }

    public List<PDSProductParameterDefinition> getOptional() {
        return optional;
    }

    public void setMandatory(List<PDSProductParameterDefinition> mandatory) {
        this.mandatory = mandatory;
    }

    public void setOptional(List<PDSProductParameterDefinition> optional) {
        this.optional = optional;
    }
}
