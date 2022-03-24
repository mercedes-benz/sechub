package com.mercedesbenz.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface SecHubDataConfigurationObject {

    /**
     * Resolve the unique name of this data configuration object. In JSON we just
     * define as "name", so easier to read and write
     * 
     * @return unique name of this data configuration object, never <code>null</code>
     */
    @JsonProperty("name")
    public String getUniqueName();
    
}
