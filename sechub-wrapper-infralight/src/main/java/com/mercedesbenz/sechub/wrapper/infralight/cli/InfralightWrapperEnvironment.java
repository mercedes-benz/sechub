// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.infralight.cli;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultRuntimeKeyConstants;

@Component
public class InfralightWrapperEnvironment {

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
    /* Infralight specific environment setup */
    /****************************************/
    @Value("${" + InfralightWrapperKeyConstants.KEY_PDS_INFRALIGHT_MOCKING_ENABLED + ":false}")
    private boolean mockingEnabled;

    public boolean isTrustAllCertificatesEnabled() {
        return trustAllCertificatesEnabled;
    }

    public String getSechubConfigurationModelAsJson() {
        return sechubConfigurationModelAsJson;
    }

    public String getPdsUserMessagesFolder() {
        return pdsUserMessagesFolder;
    }

    public String getPdsJobAdapterMetaDatafile() {
        return pdsJobAdapterMetaDatafile;
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
