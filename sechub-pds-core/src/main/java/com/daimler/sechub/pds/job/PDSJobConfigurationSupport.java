// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import com.daimler.sechub.pds.execution.PDSExecutionParameterEntry;

public class PDSJobConfigurationSupport {

    public static final String PARAM_KEY_TARGET_TYPE = "pds.scan.target.type";

    public static final String PARAM_KEY_PRODUCT_IDENTIFIER = "pds.config.productidentifier";

    public static final String PARAM_KEY_USE_SECHUB_STORAGE = "pds.config.use.sechub.storage";

    public static final String PARAM_KEY_SECHUB_STORAGE_PATH = "pds.config.sechub.storage.path";

    private PDSJobConfiguration configuration;

    public PDSJobConfigurationSupport(PDSJobConfiguration configuration) {
        this.configuration = configuration;
    }
    
    public boolean isSecHubStorageEnabled() {
        return isEnabled(PARAM_KEY_USE_SECHUB_STORAGE);
    }
    
    
    public String getSecHubStoragePath() {
        return getStringParameterOrNull(PARAM_KEY_SECHUB_STORAGE_PATH);
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
