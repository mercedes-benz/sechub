// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants.*;

import java.util.List;

import com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterValueConstants;
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
        return isEnabled(key, false);
    }

    public boolean isEnabled(String key, boolean defaultWhenNotSet) {
        if (configuration == null) {
            return defaultWhenNotSet;
        }
        String param = getStringParameterOrNull(key);
        if (param == null) {
            return defaultWhenNotSet;
        }
        return Boolean.parseBoolean(param);
    }

    public int getIntParameterOrDefault(String key, int defaultValue) {
        String value = getStringParameterOrNull(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
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

    public List<String> createIncludedFilePatternList() {
        return createListForParameterWithCommaSeparatedValues(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_FILEFILTER_INCLUDES);
    }

    public List<String> createExcludedFilePatternList() {
        return createListForParameterWithCommaSeparatedValues(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_FILEFILTER_EXCLUDES);
    }

    private List<String> createListForParameterWithCommaSeparatedValues(String key) {
        return SimpleStringUtils.createListForCommaSeparatedValues(getStringParameterOrNull(key));
    }

    public int getMillisecondsToWaitForNextCheck() {
        return getIntParameterOrDefault(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_CANCEL_EVENT_CHECKINTERVAL_MILLISECONDS,
                PDSDefaultParameterValueConstants.DEFAULT_TIME_TO_WAIT_IN_MILLISECONDS_FOR_SCRIPT_CANCELLATION_CHECK);
    }

    public int getSecondsToWaitForProcess() {
        return getIntParameterOrDefault(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_CANCEL_MAXIMUM_WAITTIME_SECONDS,
                PDSDefaultParameterValueConstants.DEFAULT_TIME_TO_WAIT_IN_SECONDS_FOR_SCRIPT_CANCELLATION);
    }

    public int getMinutesToWaitBeforeProductTimeOut(int defaultValue) {
        return getIntParameterOrDefault(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_PRODUCT_TIMEOUT_MINUTES, defaultValue);
    }
}
