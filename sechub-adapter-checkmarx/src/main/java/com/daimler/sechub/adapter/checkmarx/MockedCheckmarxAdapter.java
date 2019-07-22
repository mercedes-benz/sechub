// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.daimler.sechub.adapter.AdapterProfiles;
import com.daimler.sechub.adapter.mock.AbstractMockedAdapter;

@Profile(AdapterProfiles.MOCKED_PRODUCTS)
@Component
public class MockedCheckmarxAdapter extends AbstractMockedAdapter<CheckmarxAdapterContext, CheckmarxAdapterConfig> implements CheckmarxAdapter {

	/**
	 * Check config data is as written in yaml file! This will check that all params
	 * are really given to the mock - means e.g. no data missing or accidently using
	 * defaults
	 *
	 * @param config
	 */
	protected void validateConfigAsDefinedInMockYAML(CheckmarxAdapterConfig config) {
		if (! isMockSanityCheckEnabled()) {
			return;
		}
		/*
		 * the token is for the apiToken'nessus-api-token' and user id
		 * 'nessus-user-id' from application-mock.yml!
		 */
		if (!"checkmarx-password".equals(config.getPassword())) {
			throw new IllegalArgumentException(config.getPassword());
		}
		String productBaseURL = config.getProductBaseURL();

		boolean baseURLAsExpected = "https://checkmarx.mock.example.org:6011".equals(productBaseURL);
		if (!baseURLAsExpected) {
			throw new IllegalArgumentException("Checkmarx base url not as expected:" + productBaseURL);
		}
	}

}
