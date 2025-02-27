// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.netsparker;

import static com.mercedesbenz.sechub.domain.scan.product.netsparker.NetsparkerConstants.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.AbstractAdapterConfigBuilder;
import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;

@Deprecated // will be removed in future
@Component
public class NetsparkerInstallSetupImpl implements NetsparkerInstallSetup {

    @Value("${sechub.adapter.netsparker.intranet.agentgroupname:deprecated}")
    @MustBeDocumented(value = "The name of the agent group to be used by netsparker for *intranet scans*. If not set no agent group will be used.", scope = SCOPE_NETSPARKER)
    private String intranetAgentGroupName;

    @Value("${sechub.adapter.netsparker.internet.agentgroupname:deprecated}")
    @MustBeDocumented(value = "The name of the agent group to be used by netsparker for *intranet scans*. If not set no agent group will be used.", scope = SCOPE_NETSPARKER)
    private String internetAgentGroupName;

    @Value("${sechub.adapter.netsparker.userid:deprecated}")
    @MustBeDocumented(value = "user id of netsparker user", secret = true, scope = SCOPE_NETSPARKER)
    String netsparkerUserId;

    @Value("${sechub.adapter.netsparker.apitoken:deprecated}")
    @MustBeDocumented(value = "API token for netsparker user", secret = true, scope = SCOPE_NETSPARKER)
    String netsparkerAPIToken;

    @Value("${sechub.adapter.netsparker.baseurl:deprecated}")
    @MustBeDocumented(value = "Base url for netsparker installation", scope = SCOPE_NETSPARKER)
    String netsparkerBaseURL;

    @Value("${sechub.adapter.netsparker.defaultpolicyid:deprecated}")
    @MustBeDocumented(value = "Default policy ID for netsparker scans", scope = SCOPE_NETSPARKER)
    private String defaultPolicyId;

    @Value("${sechub.adapter.netsparker.trustall:true}")
    @MustBeDocumented(value = AbstractAdapterConfigBuilder.DOCUMENT_INFO_TRUSTALL, scope = SCOPE_NETSPARKER)
    private boolean trustAllCertificatesNecessary;

    @Value("${sechub.adapter.netsparker.licenseid:deprecated}")
    @MustBeDocumented(scope = SCOPE_NETSPARKER)
    String netsparkerLicenseId;

    @Value("${sechub.adapter.netsparker.agentname:deprecated}")
    @MustBeDocumented(value = "The name of the agent to be used by netsparker. If a agent group name is already defined the group will be superiour. If no group set and no agent name netsparker will use a agent but seems to be unpredictable which agent will be used.", scope = SCOPE_NETSPARKER)
    private String agentName;

    @Value("${sechub.adapter.netsparker.scanresultcheck.period.minutes:-1}")
    @MustBeDocumented(value = AbstractAdapterConfigBuilder.DOCUMENT_INFO_TIMEOUT_IN_MINUTES, scope = SCOPE_NETSPARKER)
    private int scanResultCheckPeriodInMinutes;

    @Value("${sechub.adapter.netsparker.scanresultcheck.timeout.minutes:-1}")
    @MustBeDocumented(value = AbstractAdapterConfigBuilder.DOCUMENT_INFO_CHECK_IN_MINUTES, scope = SCOPE_NETSPARKER)
    private int scanResultCheckTimeOutInMinutes;

    public int getScanResultCheckPeriodInMinutes() {
        return scanResultCheckPeriodInMinutes;
    }

    public boolean isTrustAllCertificatesNecessary() {
        return trustAllCertificatesNecessary;
    }

    public int getScanResultCheckTimeOutInMinutes() {
        return scanResultCheckTimeOutInMinutes;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getDefaultPolicyId() {
        return defaultPolicyId;
    }

    public String getInternetAgentGroupName() {
        return internetAgentGroupName;
    }

    public String getIntranetAgentGroupName() {
        return intranetAgentGroupName;
    }

    public String getNetsparkerLicenseId() {
        return netsparkerLicenseId;
    }

    public String getIdentifierWhenInternetTarget() {
        return internetAgentGroupName;
    }

    public String getIdentifierWhenIntranetTarget() {
        return intranetAgentGroupName;
    }

    public String getBaseURLWhenInternetTarget() {
        return netsparkerBaseURL;
    }

    @Override
    public String getBaseURLWhenIntranetTarget() {
        return netsparkerBaseURL;
    }

    @Override
    public String getUsernameWhenInternetTarget() {
        return netsparkerUserId;
    }

    @Override
    public String getUsernameWhenIntranetTarget() {
        return netsparkerUserId;
    }

    @Override
    public String getPasswordWhenInternetTarget() {
        return netsparkerAPIToken;
    }

    @Override
    public String getPasswordWhenIntranetTarget() {
        return netsparkerAPIToken;
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