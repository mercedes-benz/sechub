// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.nessus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;
import com.daimler.sechub.domain.scan.AbstractTargetIdentifyingMultiInstallSetup;
import com.daimler.sechub.domain.scan.TargetType;
import com.daimler.sechub.sharedkernel.MustBeDocumented;

@Component
public class NessusInstallSetupImpl extends AbstractTargetIdentifyingMultiInstallSetup implements NessusInstallSetup{

	@Value("${sechub.adapter.nessus.intranet.baseurl:}")
	@MustBeDocumented("Base url of nessus used for intranet scans")
	private String intranetNessusBaseURL;

	@Value("${sechub.adapter.nessus.internet.baseurl:}")
	@MustBeDocumented("Base url of nessus used for internet scans")
	private String internetNessusBaseURL;

	@Value("${sechub.adapter.nessus.intranet.userid:}")
	@MustBeDocumented(value = "User id of nessus user (intranet)",secret=true)
	private String intranetNessusUserId;

	@Value("${sechub.adapter.nessus.internet.userid:}")
	@MustBeDocumented(value = "User id of nessus user (internet)",secret=true)
	private String internetNessusUserId;

	@Value("${sechub.adapter.nessus.intranet.password:}")
	@MustBeDocumented(value="Password for nessus instance used for intranet scans",secret=true)
	private String intranetNessusPassword;

	@Value("${sechub.adapter.nessus.internet.password:}")
	@MustBeDocumented(value="Password for nessus instance used for internet scans",secret=true)
	private String internetNessusPassword;

	@Value("${sechub.adapter.nessus.defaultpolicyid}")
	@MustBeDocumented("Default policy ID for nessus scans")
	private String defaultPolicyId;

	@Value("${sechub.adapter.nessus.trustall:false}")
	@MustBeDocumented(AbstractAdapterConfigBuilder.DOCUMENT_INFO_TRUSTALL)
	private boolean trustAllCertificatesNecessary;
	
	@Override
	protected String getBaseURLWhenInternetTarget() {
		return internetNessusBaseURL;
	}
	
	public String getDefaultPolicyId() {
		return defaultPolicyId;
	}

	@Override
	protected String getBaseURLWhenIntranetTarget() {
		return intranetNessusBaseURL;
	}

	@Override
	protected String getUsernameWhenInternetTarget() {
		return internetNessusUserId;
	}

	@Override
	protected String getUsernameWhenIntranetTarget() {
		return intranetNessusUserId;
	}

	@Override
	protected String getPasswordWhenInternetTarget() {
		return internetNessusPassword;
	}

	@Override
	protected String getPasswordWhenIntranetTarget() {
		return intranetNessusPassword;
	}

	@Override
	public boolean isHavingUntrustedCertificate(TargetType target) {
		return trustAllCertificatesNecessary;
	}

}