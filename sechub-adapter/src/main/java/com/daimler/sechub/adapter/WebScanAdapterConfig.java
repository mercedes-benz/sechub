// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

public interface WebScanAdapterConfig extends AdapterConfig{

	public LoginConfig getLoginConfig();
	
	public SecHubTimeUnitData getMaxScanDuration();
	
	public boolean hasMaxScanDuration();
}
