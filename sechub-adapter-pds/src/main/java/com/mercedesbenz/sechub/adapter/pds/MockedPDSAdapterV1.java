// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import com.mercedesbenz.sechub.adapter.mock.AbstractMockedAdapter;
import com.mercedesbenz.sechub.adapter.mock.MockedAdapterSetupService;

/**
 * Special mocked adapter. It is not marked as component, so not collected by
 * spring. See {@link DelegatingMockablePDSAdapterV1} for more details
 *
 * @author Albert Tregnaghi
 *
 */
public class MockedPDSAdapterV1 extends AbstractMockedAdapter<PDSAdapterContext, PDSAdapterConfig> implements PDSAdapter {

    public MockedPDSAdapterV1(MockedAdapterSetupService setupService) {
        this.setupService = setupService;
    }

    protected void executeMockSanityCheck(PDSAdapterConfig config) {
    }

    @Override
    public int getAdapterVersion() {
        return 1;
    }

}
