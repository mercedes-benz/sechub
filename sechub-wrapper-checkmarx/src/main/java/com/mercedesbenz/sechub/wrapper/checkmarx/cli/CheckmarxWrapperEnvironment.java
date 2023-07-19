// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.checkmarx.cli;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxConstants;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultRuntimeKeyConstants;

@Component
public class CheckmarxWrapperEnvironment {

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

}
