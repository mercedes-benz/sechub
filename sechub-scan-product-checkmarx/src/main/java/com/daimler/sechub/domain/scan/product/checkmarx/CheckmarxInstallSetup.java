// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.checkmarx;

import com.daimler.sechub.domain.scan.AnyTargetOneInstallSetup;

public interface CheckmarxInstallSetup extends AnyTargetOneInstallSetup{

	public String getTeamIdForNewProjects();

}
