// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.MustBeDocumented;

@Component
public class AdministrationEnvironment {
	
	@Value("${sechub.server.administration.baseurl:${sechub.server.baseurl}}")
	@MustBeDocumented(value="Base url for server - e.g. https://sechub.example.org. ")
	String baseURL;
	
	@Value("${sechub.server.administration.baseurl:${sechub.server.baseurl}}")
	@MustBeDocumented(value="Base url for administration server - e.g. https://sechub.example.org. When users will have emails with links to sechub server this value will be used as base.")
	String administrationBaseURL;
	
	public String getAdministrationBaseURL() {
		return administrationBaseURL;
	}
	
}
