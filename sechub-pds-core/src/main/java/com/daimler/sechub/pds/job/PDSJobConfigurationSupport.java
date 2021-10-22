// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import static com.daimler.sechub.commons.pds.PDSDefaultParameterKeyConstants.*;

import com.daimler.sechub.pds.execution.PDSExecutionParameterEntry;

public class PDSJobConfigurationSupport {

    private PDSJobConfiguration configuration;

    public PDSJobConfigurationSupport(PDSJobConfiguration configuration) {
        this.configuration = configuration;
    }

    public boolean isSecHubStorageEnabled() {
        return isEnabled(PARAM_KEY_PDS_CONFIG_USE_SECHUB_STORAGE);
    }

    public String getSecHubStoragePath() {
        return getStringParameterOrNull(PARAM_KEY_PDS_CONFIG_SECHUB_STORAGE_PATH);
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
