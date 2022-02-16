// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.nessus;

import com.daimler.sechub.domain.scan.TargetIdentifyingMultiInstallSetup;

public interface NessusInstallSetup extends TargetIdentifyingMultiInstallSetup {

    public String getDefaultPolicyId();
}
