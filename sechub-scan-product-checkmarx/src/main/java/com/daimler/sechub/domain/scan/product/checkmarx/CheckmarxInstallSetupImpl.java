// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.checkmarx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;
import com.daimler.sechub.domain.scan.AbstractInstallSetup;
import com.daimler.sechub.domain.scan.TargetType;
import com.daimler.sechub.domain.scan.config.ScanConfigService;
import com.daimler.sechub.sharedkernel.MustBeDocumented;

@Component
public class CheckmarxInstallSetupImpl extends AbstractInstallSetup implements CheckmarxInstallSetup {

	@Value("${sechub.adapter.checkmarx.newproject.teamid}")
	@MustBeDocumented(value = "Initial team ID. When a scan is started a and checkmarx project is still missing, "
			+ "a new checkmarx project will be automatically created. "
			+ "For creation a team must be assigned to the project, which cannot be done by API "
			+ "(and its not clear which users should be included etc.). "
			+ "\n\nNormally this should not be necessary, because Admins should define a team (with sechubuser inside) alraedy before.")
	String teamIdForNewProjects;

	@Value("${sechub.adapter.checkmarx.baseurl}")
	@MustBeDocumented(value = "Base url for checkmarx")
	private String baseURL;

	@Value("${sechub.adapter.checkmarx.userid}")
	@MustBeDocumented(value = "User id of checkmarx user", secret = true)
	private String userId;

	@Value("${sechub.adapter.checkmarx.password}")
	@MustBeDocumented(value = "Password of checkmarx user", secret = true)
	private String password;

	@Value("${sechub.adapter.checkmarx.trustall:false}")
	@MustBeDocumented(AbstractAdapterConfigBuilder.DOCUMENT_INFO_TRUSTALL)
	private boolean trustAllCertificatesNecessary;

	@Autowired
	ScanConfigService scanConfigService;

	@Override
	public String getBaseURL() {
		return baseURL;
	}

	@Override
	public String getUserId() {
		return userId;
	}

	@Override
	public String getPassword() {
		return password;
	}

	public String getTeamIdForNewProjects(String projectId) {
		String teamId = scanConfigService.getNamePatternIdProvider("checkmarx.newproject.teamid").getIdForName(projectId);
		if (teamId!=null) {
			return teamId;
		}
		return teamIdForNewProjects;
	}

	@Override
	public String getPresetIdForNewProjects(String projectId) {
		return scanConfigService.getNamePatternIdProvider("checkmarx.newproject.presetid").getIdForName(projectId);
	}

	@Override
	public boolean isHavingUntrustedCertificate() {
		return trustAllCertificatesNecessary;
	}

	@Override
	public final boolean isAbleToScan(TargetType type) {
		return isCode(type);
	}

	@Override
	protected void init(ScanInfo info) {
		/* we do not care - not necessary to inspect, only code is supported*/
	}


}