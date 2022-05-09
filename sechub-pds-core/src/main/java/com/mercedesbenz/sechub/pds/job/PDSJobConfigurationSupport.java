// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants.*;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.pds.execution.PDSExecutionParameterEntry;

public class PDSJobConfigurationSupport {

    private PDSJobConfiguration configuration;

    public PDSJobConfigurationSupport(PDSJobConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     *
     * @return <code>true</code> when SecHub storage shall be reused
     */
    public boolean isSecHubStorageEnabled() {
        return isEnabled(PARAM_KEY_PDS_CONFIG_USE_SECHUB_STORAGE);
    }

    /**
     *
     * @return path to sechub storage or <code>null</code> when not defined
     */
    public String getSecHubStoragePath() {
        return getStringParameterOrNull(PARAM_KEY_PDS_CONFIG_SECHUB_STORAGE_PATH);
    }

    /**
     * Resolves SecHub configuration model. Will use
     * {@link #getSecHubConfigurationModelAsJson()} and automatically try to create
     * a model instance, if JSon is not null.
     *
     * @return model instance or <code>null</code> if not set
     */
    public SecHubConfigurationModel resolveSecHubConfigurationModel() {
        String json = getSecHubConfigurationModelAsJson();
        if (json == null) {
            return null;
        }
        return SecHubScanConfiguration.createFromJSON(json);
    }

    public String getSecHubConfigurationModelAsJson() {
        return getStringParameterOrNull(PARAM_KEY_PDS_SCAN_CONFIGURATION);
    }

    public boolean isEnabled(String key) {
        if (configuration == null) {
            return false;
        }
        String param = getStringParameterOrNull(key);
        return Boolean.parseBoolean(param);
    }

    public String getStringParameterOrNull(String key) {
        if (configuration == null) {
            return null;
        }
        if (key == null) {
            return null;
        }
        for (PDSExecutionParameterEntry entry : configuration.getParameters()) {
            String foundKey = entry.getKey();
            if (key.equals(foundKey)) {
                return entry.getValue();
            }
        }
        return null;
    }

}
