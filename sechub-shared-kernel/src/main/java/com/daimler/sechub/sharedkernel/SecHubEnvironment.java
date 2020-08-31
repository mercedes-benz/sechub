// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SecHubEnvironment {

	@Value("${sechub.server.baseurl}")
	@MustBeDocumented(value="Base url of SecHub server - e.g. https://sechub.example.org")
	String serverBaseUrl;

	public String getServerBaseUrl() {
		return serverBaseUrl;
	}

}
