// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin;

public interface ConfigProvider {

	public String getApiToken();
	
	public String getUser();
	
	public String getServer();

	public int getPort();

	public void handleClientError(String statusText);
}
