// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.config;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;
import com.mercedesbenz.sechub.commons.mapping.NamePatternToIdEntry;
import com.mercedesbenz.sechub.commons.model.JSONable;

@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@MustBeKeptStable("This configuration is used by admins to have templates for their mapping configurations etc.")
public class ScanMappingConfiguration implements JSONable<ScanMappingConfiguration> {

    private String apiVersion;

    private static final ScanMappingConfiguration JSON_INITIALIZER = new ScanMappingConfiguration();

    private Map<String, List<NamePatternToIdEntry>> namePatternMappings = new TreeMap<>();

    public static ScanMappingConfiguration createFromJSON(String json) {
        return JSON_INITIALIZER.fromJSON(json);
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public Map<String, List<NamePatternToIdEntry>> getNamePatternMappings() {
        return namePatternMappings;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    @Override
    public Class<ScanMappingConfiguration> getJSONTargetClass() {
        return ScanMappingConfiguration.class;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ScanMappingConfiguration)) {
            return false;
        }
        ScanMappingConfiguration other = (ScanMappingConfiguration) obj;

        return toJSON().equals(other.toJSON());
    }

}
