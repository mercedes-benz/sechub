// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.daimler.sechub.adapter.AdapterProfiles;
import com.daimler.sechub.adapter.mock.AbstractMockedAdapter;

@Profile(AdapterProfiles.MOCKED_PRODUCTS)
@Component
public class MockedPDSAdapter extends AbstractMockedAdapter<PDSAdapterContext, PDSAdapterConfig>
		implements PDSAdapter {


	protected void executeMockSanityCheck(PDSAdapterConfig config) {
	}
	
	@Override
	public int getAdapterVersion() {
		return 1;
	}

}
