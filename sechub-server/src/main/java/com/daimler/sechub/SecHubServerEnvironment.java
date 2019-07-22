// SPDX-License-Identifier: MIT
package com.daimler.sechub;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.MustBeDocumented;

@Component
public class SecHubServerEnvironment {


	@Value("${sechub.server.baseurl}")
	@MustBeDocumented(value="Base url for sechub server - e.g. https://sechub.example.org")
	String baseUrl;
	
	public String getBaseUrl() {
		return baseUrl;
	}
	
}
