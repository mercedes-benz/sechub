// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.nessus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.AbstractAdapterConfigBuilder;
import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;

@Component
public class NessusInstallSetupImpl implements NessusInstallSetup{

    @Value("${sechub.adapter.nessus.intranet.baseurl:}")
    @MustBeDocumented("Base url of nessus used for intranet scans")
    private String intranetNessusBaseURL;

    @Value("${sechub.adapter.nessus.internet.baseurl:}")
    @MustBeDocumented("Base url of nessus used for internet scans")
    private String internetNessusBaseURL;

    @Value("${sechub.adapter.nessus.intranet.userid:}")
    @MustBeDocumented(value = "User id of nessus user (intranet)", secret = true)
    private String intranetNessusUserId;

    @Value("${sechub.adapter.nessus.internet.userid:}")
    @MustBeDocumented(value = "User id of nessus user (internet)", secret = true)
    private String internetNessusUserId;

    @Value("${sechub.adapter.nessus.intranet.password:}")
    @MustBeDocumented(value = "Password for nessus instance used for intranet scans", secret = true)
    private String intranetNessusPassword;

    @Value("${sechub.adapter.nessus.internet.password:}")
    @MustBeDocumented(value = "Password for nessus instance used for internet scans", secret = true)
    private String internetNessusPassword;

    @Value("${sechub.adapter.nessus.defaultpolicyid}")
    @MustBeDocumented("Default policy ID for nessus scans")
    private String defaultPolicyId;

    @Value("${sechub.adapter.nessus.trustall:false}")
    @MustBeDocumented(AbstractAdapterConfigBuilder.DOCUMENT_INFO_TRUSTALL)
    private boolean trustAllCertificatesNecessary;

    @Override
    public String getBaseURLWhenInternetTarget() {
        return internetNessusBaseURL;
    }

    public String getDefaultPolicyId() {
        return defaultPolicyId;
    }

    @Override
    public String getBaseURLWhenIntranetTarget() {
        return intranetNessusBaseURL;
    }

    @Override
    public String getUsernameWhenInternetTarget() {
        return internetNessusUserId;
    }

    @Override
    public String getUsernameWhenIntranetTarget() {
        return intranetNessusUserId;
    }

    @Override
    public String getPasswordWhenInternetTarget() {
        return internetNessusPassword;
    }

    @Override
    public String getPasswordWhenIntranetTarget() {
        return intranetNessusPassword;
    }

    @Override
    public String getIdentifierWhenInternetTarget() {
        return null;
    }

    @Override
    public String getIdentifierWhenIntranetTarget() {
        return null;
    }

    @Override
    public boolean isHavingUntrustedCertificateForIntranet() {
        return trustAllCertificatesNecessary;
    }

    @Override
    public boolean isHavingUntrustedCertificateForInternet() {
        return trustAllCertificatesNecessary;
    }

}