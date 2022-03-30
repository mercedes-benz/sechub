// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.netsparker;

import com.mercedesbenz.sechub.domain.scan.NetworkTargetDataProvider;

public interface NetsparkerInstallSetup extends NetworkTargetDataProvider{

    public int getScanResultCheckPeriodInMinutes();

    public int getScanResultCheckTimeOutInMinutes();

    public String getAgentName();

    public String getDefaultPolicyId();

    public String getInternetAgentGroupName();

    public String getIntranetAgentGroupName();

    public String getNetsparkerLicenseId();

}
