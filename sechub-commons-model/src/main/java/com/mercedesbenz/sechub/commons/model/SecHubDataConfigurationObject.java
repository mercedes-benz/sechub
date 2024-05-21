// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface SecHubDataConfigurationObject {

    public static final String PROPERTY_UNIQUENAME_AS_NAME = "name";

    /**
     * Resolve the unique name of this data configuration object. In JSON it is
     * defined as "name", because "name" is easier to read and write compared to
     * "uniqueName"
     *
     * @return unique name of this data configuration object, never
     *         <code>null</code>
     */
    @JsonProperty("name")
    String getUniqueName();

    Optional<SecHubRemoteDataConfiguration> getRemote();

}
