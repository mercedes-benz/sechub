// SPDX-License-Identifier: MIT
package com.daimler.sechub.client.java;

import com.daimler.sechub.commons.model.AbstractSecHubConfigurationModel;
import com.daimler.sechub.commons.model.JSONable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the configuration a users defines to start a SecHub job.
 * 
 * @author Albert Tregnaghi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
public class SecHubJobConfiguration extends AbstractSecHubConfigurationModel implements JSONable<SecHubJobConfiguration> {

    private static final SecHubJobConfiguration INITIALIZER = new SecHubJobConfiguration();

    public static SecHubJobConfiguration createFromJSON(String json) {
        return INITIALIZER.fromJSON(json);
    }

    @Override
    public Class<SecHubJobConfiguration> getJSONTargetClass() {
        return SecHubJobConfiguration.class;
    }
}