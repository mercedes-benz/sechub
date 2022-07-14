package com.mercedesbenz.sechub.wrapper.checkmarx;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxConstants;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;

@Component
public class CheckmarxWrapperEnvironment {

    @Value("${" + PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_SCRIPT_TRUSTALL_CERTIFICATES_ENABLED + ":false}")
    private boolean trustAllCertificatesEnabled;

    @Value("${pds.job.user.messages.folder}")
    private String pdsUserMessagesFolder;

    @Value("${pds.job.metadata.file:}") // This is normally injected by PDS, look at PDS documentation!
    private String pdsJobMetaDatafile;

    @Value("${" + PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_CONFIGURATION + ":}")
    private String sechubConfigurationModelAsJson;

    @Value("${" + CheckmarxParameterKeyConstants.PARAM_PDS_CHECKMARX_USER + ":unknown}")
    private String checkmarxUser;

    @Value("${" + CheckmarxParameterKeyConstants.PARAM_PDS_CHECKMARX_PASSWORD + ":unknown}")
    private String checkmarxPassword;

    @Value("${" + CheckmarxParameterKeyConstants.PARAM_PDS_CHECKMARX_BASE_URL + ":unknown}")
    private String checkmarxProductBaseURL;

    private boolean alwaysFullScanEnabled;

    private int scanResultCheckPeriodInMinutes;

    private int scanResultCheckTimoutInMinutes;

    @Value("${" + CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_TEAM_ID + "}")
    private String newProjectTeamIdMapping;

    @Value("${" + CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_PRESET_ID + "}")
    private String newProjectPresetIdMapping;

    @Value("${" + PDSDefaultParameterKeyConstants.PARAM_KEY_SECHUB_JOB_UUID + "}")
    private String sechubJobUUID;

    @Value("${" + CheckmarxParameterKeyConstants.PARAM_PDS_CHECKMARX_ENGINE_CONFIGURATION_NAME + "}")
    private String checkmarxEngineConfigurationName;

    public boolean isTrustAllCertificatesEnabled() {
        return trustAllCertificatesEnabled;
    }

    public String getSechubConfigurationModelAsJson() {
        return sechubConfigurationModelAsJson;
    }

    public String getUser() {
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
        return CheckmarxConstants.DEFAULT_CLIENT_SECRET;
    }

    public String getEngineConfigurationName() {
        return checkmarxEngineConfigurationName;
    }

    public String getSecHubJobUUID() {
        return sechubJobUUID;
    }

}
