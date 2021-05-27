package com.daimler.sechub.integrationtest.security;

import static com.daimler.sechub.integrationtest.SecurityTestHelper.*;

import java.net.URL;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.daimler.sechub.integrationtest.SecurityTestHelper;
import com.daimler.sechub.integrationtest.internal.IntegrationTestContext;

class PDSServerEncryptionTest {

    private static SecurityTestHelper securityTestHelper;

    @BeforeAll
    static void beforeAll() throws Exception {
        IntegrationTestContext context = IntegrationTestContext.get();

        String checkAlive = context.getPDSUrlBuilder().buildCheckIsAliveUrl();
        securityTestHelper = new SecurityTestHelper(new URL(checkAlive));
    }

    @Test
    void tls_1_2_must_be_accepted() throws Exception {
        securityTestHelper.assertProtocolAccepted(TLS_V1_2);

    }

    @Test
    void tls_1_3_must_be_accepted() throws Exception {
        securityTestHelper.assertProtocolAccepted(TLS_V1_3);

    }

    @Test
    void tls_1_1_is_not_accepted() throws Exception {
        securityTestHelper.assertProtocolNOTAccepted(TLS_V1_1);

    }

    @Test
    void tls_1_0_is_not_accepted() throws Exception {
        securityTestHelper.assertProtocolNOTAccepted(TLS_V1_0);

    }

}
