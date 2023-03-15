// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.netsparker;

import com.mercedesbenz.sechub.adapter.SecHubTimeUnitData;
import com.mercedesbenz.sechub.adapter.WebScanAdapterConfig;

public interface NetsparkerAdapterConfig extends WebScanAdapterConfig {

    String getLicenseID();

    String getWebsiteName();

    String getAgentName();

    String getAgentGroupName();

    SecHubTimeUnitData getMaxScanDuration();

    boolean hasAgentGroup();

    boolean hasMaxScanDuration();
}