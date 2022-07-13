package com.mercedesbenz.sechub.wrapper.checkmarx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;

@Component
public class CheckmarxWrapperEnvironment {

    private static final Logger LOG = LoggerFactory.getLogger(CheckmarxWrapperEnvironment.class);

    @Value("${" + PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_SCRIPT_TRUSTALL_CERTIFICATES_ENABLED + ":false}")
    private boolean trustAllCertificatesEnabled;

    @Value("${" + PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_CONFIGURATION + ":}")
    private String sechubConfigurationModelAsJson;

    @Value("${" + CheckmarxParameterKeyConstants.PARAM_PDS_CHECKMARX_USER + ":unknown}")
    private String checkmarxUser;

    @Value("${" + CheckmarxParameterKeyConstants.PARAM_PDS_CHECKMARX_PASSWORD + ":unknown}")
    private String checkmarxPassword;

    private String checkmarxProductBaseURL;

    private boolean alwaysFullScanEnabled;

    private int scanResultCheckPeriodInMinutes;

    private int scanResultCheckTimoutInMinutes;

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

}
