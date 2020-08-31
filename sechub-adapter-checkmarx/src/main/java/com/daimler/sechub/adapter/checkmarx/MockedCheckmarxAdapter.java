// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.daimler.sechub.adapter.AdapterProfiles;
import com.daimler.sechub.adapter.mock.AbstractMockedAdapter;

@Profile(AdapterProfiles.MOCKED_PRODUCTS)
@Component
public class MockedCheckmarxAdapter extends AbstractMockedAdapter<CheckmarxAdapterContext, CheckmarxAdapterConfig> implements CheckmarxAdapter {
    
    @Value("${sechub.adapter.checkmarx.clientsecret:"+CheckmarxConfig.DEFAULT_CLIENT_SECRET+"}")
    private String clientSecret;

    
	/**
	 * Check config data is as written in yaml file! This will check that all params
	 * are really given to the mock - means e.g. no data missing or accidently using
	 * defaults
	 *
	 * @param config
	 */
	protected void executeMockSanityCheck(CheckmarxAdapterConfig config) {
		/*
		 * the token is for the apiToken'nessus-api-token' and user id
		 * 'nessus-user-id' from application-mock.yml!
		 */
		if (!"checkmarx-password".equals(config.getPasswordOrAPIToken())) {
		    handleSanityFailure("checkmarx config password:"+config.getPasswordOrAPIToken());
		}
		if (clientSecret==null) {
		    handleSanityFailure("client secret not set in in environment!");
		}
		if (! clientSecret.equals(config.getClientSecret())) {
		    handleSanityFailure("client secret not set correctly! Expected:"+clientSecret+", but got:"+config.getClientSecret());
		}
		
		String productBaseURL = config.getProductBaseURL();

		boolean baseURLAsExpected = "https://checkmarx.mock.example.org:6011".equals(productBaseURL);
		if (!baseURLAsExpected) {
		    handleSanityFailure("Checkmarx base url not as expected:" + productBaseURL);
		}
	}

	@Override
	public int getAdapterVersion() {
		return 1;
	}

}
