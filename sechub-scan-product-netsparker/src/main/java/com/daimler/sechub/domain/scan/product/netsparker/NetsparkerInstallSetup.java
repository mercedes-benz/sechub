// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.netsparker;

import com.daimler.sechub.domain.scan.TargetIdentifiyingOneInstallSetup;

public interface NetsparkerInstallSetup extends TargetIdentifiyingOneInstallSetup {

	public int getScanResultCheckPeriodInMinutes();

	public int getScanResultCheckTimeOutInMinutes();

	public String getAgentName();

	public String getDefaultPolicyId();

	public String getInternetAgentGroupName();

	public String getIntranetAgentGroupName();

	public String getNetsparkerLicenseId();
	

}
