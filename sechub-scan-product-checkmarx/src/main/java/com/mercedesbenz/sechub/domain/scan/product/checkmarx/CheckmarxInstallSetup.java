// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.checkmarx;

import com.mercedesbenz.sechub.domain.scan.InstallSetup;

public interface CheckmarxInstallSetup extends InstallSetup {

    public boolean isHavingUntrustedCertificate();
}
