// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import com.mercedesbenz.sechub.adapter.WebScanAdapterConfig;

public interface PDSWebScanConfig extends PDSAdapterConfig, WebScanAdapterConfig {

    public String getWebsiteName();
}