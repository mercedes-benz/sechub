// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.nessus;

import com.mercedesbenz.sechub.domain.scan.TargetIdentifyingMultiInstallSetup;

public interface NessusInstallSetup extends TargetIdentifyingMultiInstallSetup {

    public String getDefaultPolicyId();
}
