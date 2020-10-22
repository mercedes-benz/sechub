// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.daimler.sechub.adapter.AbstractAdapter;
import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.AdapterProfiles;
import com.daimler.sechub.adapter.AdapterRuntimeContext;
import com.daimler.sechub.adapter.mock.MockedAdapter;

/**
 * Special adapter which CAN be mocked but is per default using real product
 * (PDS). So we can use started PDS integration test server in our tests
 * 
 * @author Albert Tregnaghi
 *
 */
@Profile(AdapterProfiles.MOCKED_PRODUCTS)
@Component
public class DelegatingMockablePDSAdapterV1 extends AbstractAdapter<PDSAdapterContext, PDSAdapterConfig> implements MockedAdapter<PDSAdapterConfig>, PDSAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(DelegatingMockablePDSAdapterV1.class);

    MockedPDSAdapterV1 mockedPdsAdapterV1;

    PDSAdapterV1 realPdsAdapterV1;

    public DelegatingMockablePDSAdapterV1() {
        /* to have both worlds (mocked_products + real_products), we instanciate this here directly */
        mockedPdsAdapterV1 = new MockedPDSAdapterV1();
        realPdsAdapterV1 = new PDSAdapterV1();
    }

    @Override
    public int getAdapterVersion() {
        return 1;
    }

    @Override
    protected String execute(PDSAdapterConfig config, AdapterRuntimeContext runtimeContext) throws AdapterException {
        String mocked = config.getJobParameters().get("mocked");
        boolean mockWanted = Boolean.parseBoolean(mocked);

        LOG.info("execution starting, using mocked={}", mockWanted);

        if (mockWanted) {
            return mockedPdsAdapterV1.execute(config, runtimeContext);
        }
        return realPdsAdapterV1.execute(config, runtimeContext);
    }

    @Override
    protected String getAPIPrefix() {
        return realPdsAdapterV1.getAPIPrefix();
    }

}
