// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface SecHubDataConfigurationUsageByName {

    /**
     * A set containing names of {@link SecHubDataConfigurationObject}. In JSON the
     * name "use" will be the correct term, which makes it more readable and easier
     * to configure in JSON.
     * 
     * @return a set containing names of data configurations which shall be used.
     *         Never <code>null</code>.
     */
    @JsonProperty("use")
    public Set<String> getNamesOfUsedDataConfigurationObjects();
    
}
