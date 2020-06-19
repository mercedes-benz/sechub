// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import com.daimler.sechub.adapter.WebScanAdapterConfig;

public interface PDSAdapterConfig extends WebScanAdapterConfig{

	String getLicenseID();

	String getWebsiteName();

	String getAgentName();

	String getAgentGroupName();

	boolean hasAgentGroup();



}