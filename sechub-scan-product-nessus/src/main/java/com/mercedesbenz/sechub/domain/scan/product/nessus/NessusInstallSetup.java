// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.nessus;

import com.mercedesbenz.sechub.domain.scan.InstallSetup;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetDataProvider;

public interface NessusInstallSetup extends InstallSetup, NetworkTargetDataProvider {

    public String getDefaultPolicyId();
}
