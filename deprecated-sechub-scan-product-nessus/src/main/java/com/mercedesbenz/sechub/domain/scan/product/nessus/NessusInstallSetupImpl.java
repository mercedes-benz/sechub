// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.nessus;

import static com.mercedesbenz.sechub.domain.scan.product.nessus.NessusConstants.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.AbstractAdapterConfigBuilder;
import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;

@Component
@Deprecated // will be removed in future
public class NessusInstallSetupImpl implements NessusInstallSetup {

    @Value("${sechub.adapter.nessus.intranet.baseurl:deprecated}")
    @MustBeDocumented(value = "Base url of nessus used for intranet scans", scope = SCOPE_NESSUS)
    private String intranetNessusBaseURL;

    @Value("${sechub.adapter.nessus.internet.baseurl:deprecated}")
    @MustBeDocumented(value = "Base url of nessus used for internet scans", scope = SCOPE_NESSUS)
    private String internetNessusBaseURL;

    @Value("${sechub.adapter.nessus.intranet.userid:deprecated}")
    @MustBeDocumented(value = "User id of nessus user (intranet)", secret = true, scope = SCOPE_NESSUS)
    private String intranetNessusUserId;

    @Value("${sechub.adapter.nessus.internet.userid:deprecated}")
    @MustBeDocumented(value = "User id of nessus user (internet)", secret = true, scope = SCOPE_NESSUS)
    private String internetNessusUserId;

    @Value("${sechub.adapter.nessus.intranet.password:deprecated}")
    @MustBeDocumented(value = "Password for nessus instance used for intranet scans", secret = true, scope = SCOPE_NESSUS)
    private String intranetNessusPassword;

    @Value("${sechub.adapter.nessus.internet.password:deprecated}")
    @MustBeDocumented(value = "Password for nessus instance used for internet scans", secret = true, scope = SCOPE_NESSUS)
    private String internetNessusPassword;

    @Value("${sechub.adapter.nessus.defaultpolicyid:deprecated}")
    @MustBeDocumented(value = "Default policy ID for nessus scans", scope = SCOPE_NESSUS)
    private String defaultPolicyId;

    @Value("${sechub.adapter.nessus.trustall:true}")
    @MustBeDocumented(value = AbstractAdapterConfigBuilder.DOCUMENT_INFO_TRUSTALL, scope = SCOPE_NESSUS)
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
    public boolean hasUntrustedCertificateWhenIntranetTarget() {
        return trustAllCertificatesNecessary;
    }

    @Override
    public boolean hasUntrustedCertificateWhenInternetTarget() {
        return trustAllCertificatesNecessary;
    }

}