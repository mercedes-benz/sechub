// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.Assert.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestContext;
import com.mercedesbenz.sechub.integrationtest.internal.TestRestHelper;
import com.mercedesbenz.sechub.test.TestURLBuilder;

public abstract class AbstractAssert {

    IntegrationTestContext getContext() {
        return IntegrationTestContext.get();
    }

    TestRestHelper getRestHelper() {
        return getContext().getSuperAdminRestHelper();
    }

    TestRestHelper getRestHelper(TestUser user) {
        return getContext().getRestHelper(user);
    }

    TestURLBuilder getUrlBuilder() {
        return getContext().getUrlBuilder();
    }

    void expectHttpClientError(HttpStatus expected, Runnable r, String errorMessage) {
        try {
            r.run();
            fail(errorMessage);
        } catch (HttpClientErrorException e) {
            if (expected != e.getStatusCode()) {
                throw new IllegalStateException("other http state than expected:" + e.getStatusCode(), e);
            }
        }
    }
}
