package com.mercedesbenz.sechub.wrapper.checkmarx.cli;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxConstants;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultRuntimeKeyConstants;
import com.mercedesbenz.sechub.wrapper.checkmarx.CheckmarxWrapperKeyConstants;

@Component
public class CheckmarxWrapperCLIEnvironment {

    @Value("${" + PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_SCRIPT_TRUSTALL_CERTIFICATES_ENABLED + ":false}")
    private boolean trustAllCertificatesEnabled;

    @Value("${" + PDSDefaultRuntimeKeyConstants.RT_KEY_PDS_USER_MESSAGES_FOLDER + "}")
    private String pdsUserMessagesFolder;

    @Value("${" + PDSDefaultRuntimeKeyConstants.RT_KEY_PDS_JOB_METADATA_FILE + ":}") // This is normally injected by PDS, look at PDS documentation!
    private String pdsJobMetaDatafile;

    @Value("${" + PDSDefaultRuntimeKeyConstants.RT_KEY_PDS_JOB_EXTRACTED_SOURCE_FOLDER + "}") // This is normally injected by PDS, look at PDS documentation!
    private String pdsExtractedSourceFolder;

    @Value("${" + PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_CONFIGURATION + "}")
    private String sechubConfigurationModelAsJson;

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_USER + "}")
    private String checkmarxUser;

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_PASSWORD + "}")
    private String checkmarxPassword;

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_BASE_URL + "}")
    private String checkmarxProductBaseURL;

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_ALWAYS_FULLSCAN_ENABLED + ":false}")
    private boolean alwaysFullScanEnabled;

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_RESULT_CHECK_PERIOD_MINUTES + ":1}")
    private int scanResultCheckPeriodInMinutes;

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_RESULT_CHECK_TIMOUT_MINUTES + ":300}")
    private int scanResultCheckTimoutInMinutes;

    @Value("${" + CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_TEAM_ID + "}")
    private String newProjectTeamIdMapping;

    @Value("${" + CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_PRESET_ID + "}")
    private String newProjectPresetIdMapping;

    @Value("${" + PDSDefaultParameterKeyConstants.PARAM_KEY_SECHUB_JOB_UUID + "}")
    private String sechubJobUUID;

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_ENGINE_CONFIGURATION_NAME + "}")
    private String checkmarxEngineConfigurationName;

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_MOCKING_ENABLED + ":false}")
    private boolean mockingEnabled;

    @Value("${" + CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_CLIENT_SECRET + ":" + CheckmarxConstants.DEFAULT_CLIENT_SECRET + "}")
    private String clientSecret;

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

    public int getScanResultCheckPeriodInMinutes() {
        return scanResultCheckPeriodInMinutes;
    }

    public int getScanResultCheckTimeOutInMinutes() {
        return scanResultCheckTimoutInMinutes;
    }

    public String getPdsUserMessagesFolder() {
        return pdsUserMessagesFolder;
    }

    public String getPdsJobMetaDatafile() {
        return pdsJobMetaDatafile;
    }

    public String getNewProjectTeamIdMapping() {
        return newProjectPresetIdMapping;
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

}
