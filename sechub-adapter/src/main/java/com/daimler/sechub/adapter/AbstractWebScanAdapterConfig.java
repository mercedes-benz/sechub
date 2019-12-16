// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

public class AbstractWebScanAdapterConfig extends AbstractAdapterConfig implements WebScanAdapterConfig{

	AbstractLoginConfig loginConfig;

	public LoginConfig getLoginConfig() {
		return loginConfig;
	}




}
