// SPDX-License-Identifier: MIT
package com.daimler.sechub.client.java;

import com.daimler.sechub.client.java.api.AnonymousApi;
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import org.junit.Test;

public class SecHubClientTest2 {

    @Test
    public void test() throws Exception {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://localhost");
        
        AnonymousApi apiInstance = new AnonymousApi(defaultClient);

        try {
            apiInstance.anonymousCheckAliveGet();
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

}
