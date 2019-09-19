// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.netsparker;

import com.daimler.sechub.adapter.WebScanAdapterConfig;

public interface NetsparkerAdapterConfig extends WebScanAdapterConfig{

	String getLicenseID();

	String getWebsiteName();

	String getAgentName();

	String getAgentGroupName();

	boolean hasAgentGroup();



}