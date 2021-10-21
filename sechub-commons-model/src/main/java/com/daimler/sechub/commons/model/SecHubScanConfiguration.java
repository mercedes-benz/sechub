// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the configuration a users defines to start scans
 * 
 * @author Albert Tregnaghi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
public class SecHubScanConfiguration extends SecHubConfigurationModel implements JSONable<SecHubScanConfiguration> {

    private static final SecHubScanConfiguration INITIALIZER = new SecHubScanConfiguration();

    public static SecHubScanConfiguration createFromJSON(String json) {
        return INITIALIZER.fromJSON(json);
    }

    @Override
    public Class<SecHubScanConfiguration> getJSONTargetClass() {
        return SecHubScanConfiguration.class;
    }
}