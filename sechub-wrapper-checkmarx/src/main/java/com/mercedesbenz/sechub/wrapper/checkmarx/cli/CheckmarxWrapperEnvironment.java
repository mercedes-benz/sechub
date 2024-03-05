// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.checkmarx.cli;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxConstants;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxResilienceConfiguration;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultRuntimeKeyConstants;

@Component
public class CheckmarxWrapperEnvironment implements CheckmarxResilienceConfiguration {

    /********************************/
    /* PDS common environment setup */
    /********************************/

    @Value("${" + PDSDefaultParameterKeyConstants.PARAM_KEY_SECHUB_JOB_UUID + "}")
    private String sechubJobUUID;

    @Value("${" + PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_SCRIPT_TRUSTALL_CERTIFICATES_ENABLED + ":false}")
    private boolean trustAllCertificatesEnabled;

    @Value("${" + PDSDefaultRuntimeKeyConstants.RT_KEY_PDS_JOB_USER_MESSAGES_FOLDER + "}")
    private String pdsUserMessagesFolder;

    @Value("${" + PDSDefaultRuntimeKeyConstants.RT_KEY_PDS_JOB_METADATA_FILE + ":}")
    private String pdsJobAdapterMetaDatafile;

    @Value("${" + PDSDefaultRuntimeKeyConstants.RT_KEY_PDS_JOB_EXTRACTED_SOURCES_FOLDER + "}")
    private String pdsExtractedSourceFolder;

    @Value("${" + PDSDefaultRuntimeKeyConstants.RT_KEY_PDS_JOB_RESULT_FILE + "}")
    private String pdsResultFile;

    @Value("${" + PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_CONFIGURATION + "}")
    private String sechubConfigurationModelAsJson;

    /****************************************/
    /* Checkmarx specific environment setup */
    /****************************************/

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_USER + "}")
    private String checkmarxUser;

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_PASSWORD + "}")
    private String checkmarxPassword;

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_BASE_URL + "}")
    private String checkmarxProductBaseURL;

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_ALWAYS_FULLSCAN_ENABLED + ":false}")
    private boolean alwaysFullScanEnabled;

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_RESULT_CHECK_PERIOD_MILLISECONDS + ":60000}")
    private int scanResultCheckPeriodInMilliseconds;

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_RESULT_CHECK_TIMOUT_MINUTES + ":300}")
    private int scanResultCheckTimoutInMinutes;

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_ENGINE_CONFIGURATION_NAME + "}")
    private String checkmarxEngineConfigurationName;

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_MOCKING_ENABLED + ":false}")
    private boolean mockingEnabled;

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_CLIENT_SECRET + ":" + CheckmarxConstants.DEFAULT_CLIENT_SECRET + "}")
    private String clientSecret;

    /**************************************************/
    /* SecHub mappings for Checkmarx as job parameter */
    /**************************************************/

    @Value("${" + CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_TEAM_ID + "}")
    private String newProjectTeamIdMapping;

    @Value("${" + CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_PRESET_ID + ":}")
    private String newProjectPresetIdMapping;

    /**************************************************/
    /* Checkmarx adapter resilience settings */
    /**************************************************/

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_RESILIENCE_BAD_REQUEST_MAX_RETRIES + ":" + DEFAULT_BADREQUEST_RETRY_MAX + "}")
    private int badRequestMaxRetries = DEFAULT_BADREQUEST_RETRY_MAX;

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_RESILIENCE_BAD_REQUEST_RETRY_WAIT_MILLISECONDS + ":"
            + DEFAULT_BADREQUEST_RETRY_TIME_TO_WAIT_MILLISECONDS + "}")
    private int badRequestRetryTimeToWaitInMilliseconds = DEFAULT_BADREQUEST_RETRY_TIME_TO_WAIT_MILLISECONDS;

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_RESILIENCE_SERVER_ERROR_MAX_RETRIES + ":" + DEFAULT_SERVERERROR_RETRY_MAX + "}")
    private int internalServerErrortMaxRetries = DEFAULT_SERVERERROR_RETRY_MAX;

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_RESILIENCE_SERVER_ERROR_RETRY_WAIT_MILLISECONDS + ":"
            + DEFAULT_SERVERERROR_RETRY_TIME_TO_WAIT_MILLISECONDS + "}")
    private int internalServerErrorTimeToWaitInMilliseconds = DEFAULT_SERVERERROR_RETRY_TIME_TO_WAIT_MILLISECONDS;

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_RESILIENCE_NETWORK_ERROR_MAX_RETRIES + ":" + DEFAULT_NETWORKERROR_RETRY_MAX + "}")
    private int networkErrorMaxRetries;

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_RESILIENCE_NETWORK_EROR_RETRY_WAIT_MILLISECONDS + ":"
            + DEFAULT_NETWORKERROR_RETRY_TIME_TO_WAIT_MILLISECONDS + "}")
    private int networkErrorRetryTimeToWaitInMilliseconds;

    public boolean isTrustAllCertificatesEnabled() {
        return trustAllCertificatesEnabled;
    }

    public String getSechubConfigurationModelAsJson() {
        return sechubConfigurationModelAsJson;
    }

    public String getCheckmarxUser() {
        return checkmarxUser;
    }

    public String getCheckmarxPassword() {
        return checkmarxPassword;
    }

    public String getCheckmarxProductBaseURL() {
        return checkmarxProductBaseURL;
    }

    public boolean isAlwaysFullScanEnabled() {
        return alwaysFullScanEnabled;
    }

    public int getScanResultCheckPeriodInMilliseconds() {
        return scanResultCheckPeriodInMilliseconds;
    }

    public int getScanResultCheckTimeOutInMinutes() {
        return scanResultCheckTimoutInMinutes;
    }

    public String getPdsUserMessagesFolder() {
        return pdsUserMessagesFolder;
    }

    public String getPdsJobAdapterMetaDatafile() {
        return pdsJobAdapterMetaDatafile;
    }

    public String getNewProjectTeamIdMapping() {
        return newProjectTeamIdMapping;
    }

    public String getNewProjectPresetIdMapping() {
        return newProjectPresetIdMapping;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getEngineConfigurationName() {
        return checkmarxEngineConfigurationName;
    }

    public String getSecHubJobUUID() {
        return sechubJobUUID;
    }

    public boolean isMockingEnabled() {
        return mockingEnabled;
    }

    public String getPdsJobExtractedSourceFolder() {
        return pdsExtractedSourceFolder;
    }

    public String getPdsResultFile() {
        return pdsResultFile;
    }

    public int getBadRequestMaxRetries() {
        return badRequestMaxRetries;
    }

    public int getBadRequestRetryTimeToWaitInMilliseconds() {
        return badRequestRetryTimeToWaitInMilliseconds;
    }

    public int getInternalServerErrortMaxRetries() {
        return internalServerErrortMaxRetries;
    }

    public int getInternalServerErrorRetryTimeToWaitInMilliseconds() {
        return internalServerErrorTimeToWaitInMilliseconds;
    }

    public int getNetworkErrorMaxRetries() {
        return networkErrorMaxRetries;
    }

    public int getNetworkErrorRetryTimeToWaitInMilliseconds() {
        return networkErrorRetryTimeToWaitInMilliseconds;

    }
}
