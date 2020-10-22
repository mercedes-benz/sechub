// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import com.daimler.sechub.adapter.mock.AbstractMockedAdapter;

// no component, so not collected by spring
public class MockedPDSAdapterV1 extends AbstractMockedAdapter<PDSAdapterContext, PDSAdapterConfig>
		implements PDSAdapter {


	protected void executeMockSanityCheck(PDSAdapterConfig config) {
	}
	
	@Override
	public int getAdapterVersion() {
		return 1;
	}

}
