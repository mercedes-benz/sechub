// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.netsparker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;
import com.daimler.sechub.domain.scan.AbstractTargetIdentifyingOneInstallSetup;
import com.daimler.sechub.sharedkernel.MustBeDocumented;

@Component
public class NetsparkerInstallSetupImpl extends AbstractTargetIdentifyingOneInstallSetup implements NetsparkerInstallSetup{

	@Value("${sechub.adapter.netsparker.intranet.agentgroupname:}")
	@MustBeDocumented("The name of the agent group to be used by netsparker for *intranet scans*. If not set no agent group will be used.")
	private String intranetAgentGroupName;

	@Value("${sechub.adapter.netsparker.internet.agentgroupname:}")
	@MustBeDocumented("The name of the agent group to be used by netsparker for *intranet scans*. If not set no agent group will be used.")
	private String internetAgentGroupName;

	@Value("${sechub.adapter.netsparker.userid}")
	@MustBeDocumented(value="user id of netsparker user",secret=true)
	String netsparkerUserId;

	@Value("${sechub.adapter.netsparker.apitoken}")
	@MustBeDocumented(value="API token for netsparker user",secret=true)
	String netsparkerAPIToken;
	
	@Value("${sechub.adapter.netsparker.baseurl}")
	@MustBeDocumented(value="Base url for netsparker installation")
	String netsparkerBaseURL;
	
	@Value("${sechub.adapter.netsparker.defaultpolicyid}")
	@MustBeDocumented("Default policy ID for netsparker scans")
	private String defaultPolicyId;
	
	@Value("${sechub.adapter.netsparker.trustall:false}")
	@MustBeDocumented(AbstractAdapterConfigBuilder.DOCUMENT_INFO_TRUSTALL)
	private boolean trustAllCertificatesNecessary;

	@Value("${sechub.adapter.netsparker.licenseid}")
	@MustBeDocumented
	String netsparkerLicenseId;

	@Value("${sechub.adapter.netsparker.agentname:}")
	@MustBeDocumented("The name of the agent to be used by netsparker. If a agent group name is already defined the group will be superiour. If no group set and no agent name netsparker will use a agent but seems to be unpredictable which agent will be used.")
	private String agentName;

	@Value("${sechub.adapter.netsparker.scanresultcheck.period.minutes:-1}")
	@MustBeDocumented(AbstractAdapterConfigBuilder.DOCUMENT_INFO_TIMEOUT)
	private int scanResultCheckPeriodInMinutes;

	@Value("${sechub.adapter.netsparker.scanresultcheck.timeout.minutes:-1}")
	@MustBeDocumented(AbstractAdapterConfigBuilder.DOCUMENT_INFO_CHECK)
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
	
	@Override
	protected String getIdentifierWhenInternetTarget() {
		return internetAgentGroupName;
	}

	@Override
	protected String getIdentifierWhenIntranetTarget() {
		return intranetAgentGroupName;
	}

	@Override
	public String getUserId() {
		return netsparkerUserId;
	}

	@Override
	public String getPassword() {
		return netsparkerAPIToken;
	}
	
	@Override
	public String getBaseURL() {
		return netsparkerBaseURL;
	}

	@Override
	public boolean isHavingUntrustedCertificate() {
		return trustAllCertificatesNecessary;
	}

}