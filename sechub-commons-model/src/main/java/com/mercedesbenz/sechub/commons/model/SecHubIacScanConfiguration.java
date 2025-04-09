// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubIacScanConfiguration implements SecHubDataConfigurationUsageByName {

    private Set<String> namesOfUsedDataConfigurationObjects = new LinkedHashSet<>();

    @Override
    public Set<String> getNamesOfUsedDataConfigurationObjects() {
        return namesOfUsedDataConfigurationObjects;
    }

}
