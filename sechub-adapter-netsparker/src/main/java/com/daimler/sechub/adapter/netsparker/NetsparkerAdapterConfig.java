// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.netsparker;

import com.daimler.sechub.adapter.AdapterConfig;

public interface NetsparkerAdapterConfig extends AdapterConfig{

	String getLicenseID();

	String getWebsiteName();

	String getAgentName();

	String getAgentGroupName();

	boolean hasAgentGroup();

	

}