// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.model.JSONable;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.sharedkernel.MustBeKeptStable;

/**
 * Be aware to add only parts into this class and do NOT remove properties being
 * still in PROD! (E.g. API V1 stills supported has field "ugly" and API V2 does
 * not support it, but API V1 is still in use... and supported) If you dont
 * support a field in a special API variant you should trigger an error in
 * validation!
 *
 * @author Albert Tregnaghi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@MustBeKeptStable("This configuration is used by users to schedule a job. It has to be backward compatible. To afford this we will NOT remove older parts since final API releases")
public class SecHubConfiguration extends SecHubConfigurationModel implements JSONable<SecHubConfiguration> {

    private static final SecHubConfiguration INITIALIZER = new SecHubConfiguration();

    public static SecHubConfigurationModel createFromJSON(String json) {
        return INITIALIZER.fromJSON(json);
    }

    @Override
    public Class<SecHubConfiguration> getJSONTargetClass() {
        return SecHubConfiguration.class;
    }
}
