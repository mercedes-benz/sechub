// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationType;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationTypeListParser;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterValueConstants;
import com.mercedesbenz.sechub.commons.pds.data.PDSTemplateMetaData;
import com.mercedesbenz.sechub.pds.execution.PDSExecutionParameterEntry;

public class PDSJobConfigurationSupport {

    private static final int MINIMUM_RETRIES = 0;
    private static final int MINIMUM_WAIT_SECONDS = 1;
    public static final Set<SecHubDataConfigurationType> FALLBACK_ALL_DATATYPES_SUPPORTED = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(SecHubDataConfigurationType.values())));
    private static final SecHubDataConfigurationTypeListParser DEFAULT_SHARED_TYPELIST_PARSER = new SecHubDataConfigurationTypeListParser();
    private static final Logger LOG = LoggerFactory.getLogger(PDSJobConfigurationSupport.class);

    SecHubDataConfigurationTypeListParser typeListParser;
    private PDSJobConfiguration jobConfiguration;

    public PDSJobConfigurationSupport(PDSJobConfiguration configuration) {
        this.jobConfiguration = configuration;
    }

    private SecHubDataConfigurationTypeListParser getTypeListParser() {
        if (typeListParser == null) {
            return DEFAULT_SHARED_TYPELIST_PARSER;
        }
        return typeListParser;
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
        if (jobConfiguration == null) {
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
        if (jobConfiguration == null) {
            return null;
        }
        if (key == null) {
            return null;
        }
        for (PDSExecutionParameterEntry entry : jobConfiguration.getParameters()) {
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

    public String getProductId() {
        return jobConfiguration.getProductId();
    }

    /**
     * Resolves supported data types. Will inspect the job parameter value from the
     * job configuration. If not available, the default value will be used (if
     * existing). If still not defined, the fallback set
     * {@link #FALLBACK_ALL_DATATYPES_SUPPORTED} will be returned.
     *
     * @param defaultValue, can be <code>null</code> or empty
     * @return data types set, never <code>null</code>
     */
    public Set<SecHubDataConfigurationType> getSupportedDataTypes(String defaultValue) {
        String value = getStringParameterOrNull(PARAM_KEY_PDS_CONFIG_SUPPORTED_DATATYPES);
        if (value == null || value.trim().isEmpty()) {
            value = defaultValue;
        }

        Set<SecHubDataConfigurationType> fromParser = getTypeListParser().fetchTypesAsSetOrNull(value);
        if (fromParser == null) {
            LOG.warn("Illegal situation - no supported data types found. Switch to fallback.");
            return FALLBACK_ALL_DATATYPES_SUPPORTED;
        }

        return fromParser;
    }

    public int getJobStorageReadResilienceRetriesMax(int defaultValue) {
        String key = PARAM_KEY_PDS_CONFIG_JOBSTORAGE_READ_RESILIENCE_RETRIES_MAX;

        int result = getIntParameterOrDefault(key, defaultValue);

        if (result < MINIMUM_RETRIES) {
            LOG.warn("Configured value for: {} is too small: {} - falling back to minimum: {}", key, result, MINIMUM_RETRIES);
            return MINIMUM_RETRIES;
        }
        return result;
    }

    public int getJobStorageReadResiliencRetryWaitSeconds(int defaultValue) {
        String key = PARAM_KEY_PDS_CONFIG_JOBSTORAGE_READ_RESILIENCE_RETRY_WAIT_SECONDS;

        int result = getIntParameterOrDefault(key, defaultValue);

        if (result < MINIMUM_WAIT_SECONDS) {
            LOG.warn("Configured value for: {} is too small: {} - falling back to minimum: {}", key, result, MINIMUM_WAIT_SECONDS);
            return MINIMUM_WAIT_SECONDS;
        }
        return result;
    }

    public List<PDSTemplateMetaData> getTemplateMetaData() {

        String json = getStringParameterOrNull(PARAM_KEY_PDS_CONFIG_TEMPLATE_META_DATA_LIST);
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        return JSONConverter.get().fromJSONtoListOf(PDSTemplateMetaData.class, json);
    }

}
