// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

import com.daimler.sechub.domain.scan.InstallSetup;

public interface PDSInstallSetup extends InstallSetup {

	public int getDefaultScanResultCheckPeriodInMinutes();

	public int getScanResultCheckTimeOutInMinutes();

}
