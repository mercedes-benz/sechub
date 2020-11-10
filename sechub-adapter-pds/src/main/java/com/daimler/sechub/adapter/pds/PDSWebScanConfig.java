// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import com.daimler.sechub.adapter.WebScanAdapterConfig;

public interface PDSWebScanConfig extends PDSAdapterConfig, WebScanAdapterConfig{

    public String getWebsiteName();
}
