// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubWebScanApiConfiguration implements SecHubDataConfigurationUsageByName {

    public static final String PROPERTY_TYPE = "type";

    private SecHubWebScanApiType type;

    public SecHubWebScanApiType getType() {
        return type;
    }

    public void setType(SecHubWebScanApiType type) {
        this.type = type;
    }

    private Set<String> namesOfUsedDataConfigurationObjects = new LinkedHashSet<>();

    @Override
    public Set<String> getNamesOfUsedDataConfigurationObjects() {
        return namesOfUsedDataConfigurationObjects;
    }
}
