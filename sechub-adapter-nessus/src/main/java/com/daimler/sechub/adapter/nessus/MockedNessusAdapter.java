// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.nessus;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.daimler.sechub.adapter.AdapterProfiles;
import com.daimler.sechub.adapter.mock.AbstractMockedAdapter;
import com.daimler.sechub.adapter.mock.MockedAdapter;

@Profile(AdapterProfiles.MOCKED_PRODUCTS)
@Component
public class MockedNessusAdapter extends AbstractMockedAdapter<NessusAdapterContext, NessusAdapterConfig> implements NessusAdapter, MockedAdapter<NessusAdapterConfig> {

	protected void validateConfigAsDefinedInMockYAML(NessusAdapterConfig config) {
		/*
		 * the token is for the apiToken'nessus-api-token' and user id
		 * 'nessus-user-id' from application-mock.yml!
		 */
		if (!"nessus-password".equals(config.getPasswordOrAPIToken())) {
			throw new IllegalArgumentException(config.getPasswordOrAPIToken());
		}
		if (!"nessus-default-policiy-id".equals(config.getPolicyId())) {
			throw new IllegalArgumentException("Nessus policy not as expected:" + config.getPolicyId());
		}
		String productBaseURL = config.getProductBaseURL();

		boolean baseURLAsExpected = "https://nessus-intranet.mock.example.org:6000".equals(productBaseURL);
		baseURLAsExpected= baseURLAsExpected || "https://nessus-internet.mock.example.org.com:6000".equals(productBaseURL);
		if (!baseURLAsExpected) {
			throw new IllegalArgumentException("Nessus base url not as expected:" + productBaseURL);
		}
	}
	
	@Override
	public int getAdapterVersion() {
		return 1;
	}


}
