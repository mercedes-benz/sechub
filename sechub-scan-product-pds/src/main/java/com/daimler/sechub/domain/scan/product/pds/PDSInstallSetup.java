// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

import com.daimler.sechub.domain.scan.TargetIdentifiyingOneInstallSetup;

public interface PDSInstallSetup extends TargetIdentifiyingOneInstallSetup {

	public int getScanResultCheckPeriodInMinutes();

	public int getScanResultCheckTimeOutInMinutes();

	public String getAgentName();

	public String getDefaultPolicyId();

	public String getInternetAgentGroupName();

	public String getIntranetAgentGroupName();

	public String getNetsparkerLicenseId();
	

}
