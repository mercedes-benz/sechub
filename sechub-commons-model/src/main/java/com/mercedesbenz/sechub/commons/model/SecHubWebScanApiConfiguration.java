// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubWebScanApiConfiguration implements SecHubDataConfigurationUsageByName {

    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_API_DEFINITION_URL = "apiDefinitionUrl";

    private SecHubWebScanApiType type;
    private Set<String> namesOfUsedDataConfigurationObjects = new LinkedHashSet<>();

    private URL apiDefinitionUrl;

    public SecHubWebScanApiType getType() {
        return type;
    }

    public void setType(SecHubWebScanApiType type) {
        this.type = type;
    }

    @Override
    public Set<String> getNamesOfUsedDataConfigurationObjects() {
        return namesOfUsedDataConfigurationObjects;
    }

    public URL getApiDefinitionUrl() {
        return apiDefinitionUrl;
    }

    public void setApiDefinitionUrl(URL apiDefinitionUrl) {
        this.apiDefinitionUrl = apiDefinitionUrl;
    }

}
