// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

import com.daimler.sechub.domain.scan.InstallSetup;

public interface PDSInstallSetup extends InstallSetup {

    /**
     * Resolves amount of milliseconds to wait before next check for scan result
     * from PDS server will be done.
     * 
     * @return default check period time in minutes
     */
    public int getDefaultTimeToWaitForNextCheckOperationInMilliseconds();

    /**
     * Resolves default timeout in minutes when PDS scan will terminate.
     * 
     * @return default timeout in minutes
     */
    public int getDefaultTimeOutInMinutes();

}
