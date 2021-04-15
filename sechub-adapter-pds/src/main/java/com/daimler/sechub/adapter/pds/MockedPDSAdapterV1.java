// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import com.daimler.sechub.adapter.mock.AbstractMockedAdapter;

/**
 * Special mocked adapter. It is not marked as component, so not collected by spring. See {@link DelegatingMockablePDSAdapterV1}
 * for more details 
 * 
 * @author Albert Tregnaghi
 *
 */
public class MockedPDSAdapterV1 extends AbstractMockedAdapter<PDSAdapterContext, PDSAdapterConfig>
		implements PDSAdapter {


	protected void executeMockSanityCheck(PDSAdapterConfig config) {
	}
	
	@Override
	public int getAdapterVersion() {
		return 1;
	}

}
