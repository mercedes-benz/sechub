// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.checkmarx;

import com.daimler.sechub.domain.scan.InstallSetup;

public interface CheckmarxInstallSetup extends InstallSetup{

    /**
     * Get 'client secret' value
     * 
     * @return
     */
    public String getClientSecret();

    public boolean isHavingUntrustedCertificate();
}
