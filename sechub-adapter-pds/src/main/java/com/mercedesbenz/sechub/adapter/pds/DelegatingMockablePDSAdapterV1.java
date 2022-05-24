// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.AbstractAdapter;
import com.mercedesbenz.sechub.adapter.AdapterException;
import com.mercedesbenz.sechub.adapter.AdapterProfiles;
import com.mercedesbenz.sechub.adapter.AdapterRuntimeContext;
import com.mercedesbenz.sechub.adapter.mock.MockedAdapter;
import com.mercedesbenz.sechub.adapter.mock.MockedAdapterSetupService;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;

/**
 * Special adapter which is per default mocked, but can be defined to use real
 * product (PDS). So we can use started PDS integration test server in our tests
 *
 * @author Albert Tregnaghi
 *
 */
@Profile(AdapterProfiles.MOCKED_PRODUCTS)
@Component
public class DelegatingMockablePDSAdapterV1 extends AbstractAdapter<PDSAdapterContext, PDSAdapterConfig>
        implements MockedAdapter<PDSAdapterConfig>, PDSAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(DelegatingMockablePDSAdapterV1.class);

    MockedPDSAdapterV1 mockedPdsAdapterV1;

    PDSAdapterV1 realPdsAdapterV1;

    @Autowired
    public DelegatingMockablePDSAdapterV1(MockedAdapterSetupService setupService) {
        /*
         * to have both worlds (mocked_products + real_products), we instantiate this
         * here directly
         */
        mockedPdsAdapterV1 = new MockedPDSAdapterV1(setupService);
        realPdsAdapterV1 = new PDSAdapterV1();
    }

    @Override
    public int getAdapterVersion() {
        return 1;
    }

    @Override
    protected String execute(PDSAdapterConfig config, AdapterRuntimeContext runtimeContext) throws AdapterException {
        PDSAdapterConfigData data = config.getPDSAdapterConfigData();
        String mockingDisabled = data.getJobParameters().get(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_MOCKING_DISABLED);

        boolean useMock = !Boolean.parseBoolean(mockingDisabled);

        LOG.info("execution starting, using mocked adapter={}", useMock);

        if (useMock) {
            return mockedPdsAdapterV1.execute(config, runtimeContext);
        }
        return realPdsAdapterV1.execute(config, runtimeContext);
    }

    @Override
    protected String getAPIPrefix() {
        return realPdsAdapterV1.getAPIPrefix();
    }

}
