// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.security;

import static com.mercedesbenz.sechub.integrationtest.SecurityTestHelper.*;

import java.net.URL;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import com.mercedesbenz.sechub.integrationtest.SecurityTestHelper;
import com.mercedesbenz.sechub.integrationtest.SecurityTestHelper.TestTargetType;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestContext;

/* We need a running SecHub Server instance - execute this test only when integration test server is running:*/
@EnabledIfSystemProperty(named = "sechub.integrationtest.running", matches = "true")
class ServerEncryptionIntTest {

    private static SecurityTestHelper securityTestHelper;

    @BeforeAll
    static void beforeAll() throws Exception {
        IntegrationTestContext context = IntegrationTestContext.get();

        String checkAlive = context.getUrlBuilder().buildCheckIsAliveUrl();
        securityTestHelper = new SecurityTestHelper(TestTargetType.SECHUB_SERVER, new URL(checkAlive));
    }

    @Test
    void verified_ciphers_do_not_have_MAC_with_SHA_or_SHA1() throws Exception {
        securityTestHelper.assertNotContainedMacsInCiphers("SHA", "SHA1");
    }

    @Test
    void verified_ciphers_do_not_have_MAC_with_MD5() throws Exception {
        securityTestHelper.assertNotContainedMacsInCiphers("MD5");
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

    @Test
    void sslv3_is_not_accepted() throws Exception {
        securityTestHelper.assertProtocolNOTAccepted(SSL_V3);
    }

}
