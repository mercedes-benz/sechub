// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.netsparker;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.daimler.sechub.adapter.AdapterProfiles;
import com.daimler.sechub.adapter.mock.AbstractMockedAdapter;

@Profile(AdapterProfiles.MOCKED_PRODUCTS)
@Component
public class MockedNetsparkerAdapter extends AbstractMockedAdapter<NetsparkerAdapterContext, NetsparkerAdapterConfig> implements NetsparkerAdapter {

    protected void executeMockSanityCheck(NetsparkerAdapterConfig config) {
        String productBaseURL = config.getProductBaseURL();
        boolean baseURLAsExpected = "https://netsparker.mock.example.org:4000".equals(productBaseURL);
        if (!baseURLAsExpected) {
            throw new IllegalArgumentException("Netsparker base url not as expected:" + productBaseURL);
        }
        /*
         * the token is for the apiToken'netsparker-api-token' and user id
         * 'netsparker-user-id' from application-mock.yml!
         */
        if (!"bmV0c3Bhcmtlci11c2VyLWlkOm5ldHNwYXJrZXItYXBpLXRva2Vu".equals(config.getCredentialsBase64Encoded())) {
            handleSanityFailure("pwd not as expected" + config.getCredentialsBase64Encoded());
        }
        if (!"netsparker-default-policiy-id".equals(config.getPolicyId())) {
            handleSanityFailure("Netsparker policy not as expected:" + config.getPolicyId());
        }
        if (!"netsparker-license-id".equals(config.getLicenseID())) {
            handleSanityFailure("netsparker-license-id not as expected:" + config.getLicenseID());
        }
        String agentGroupName = config.getAgentGroupName();
        boolean agentGroupAsExpected = "netsparker-agent-group-intranet".equals(agentGroupName);
        agentGroupAsExpected = agentGroupAsExpected || "netsparker-agent-group-internet".equals(agentGroupName);
        if (!agentGroupAsExpected) {
            handleSanityFailure("netsparker agent group name not found but:" + agentGroupName);
        }
    }

    @Override
    public int getAdapterVersion() {
        return 1;
    }

}
