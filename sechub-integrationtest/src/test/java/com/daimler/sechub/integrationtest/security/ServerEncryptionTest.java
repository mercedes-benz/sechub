// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.security;

import static com.daimler.sechub.integrationtest.SecurityTestHelper.*;

import java.net.URL;

import org.junit.Assume;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.daimler.sechub.integrationtest.SecurityTestHelper;
import com.daimler.sechub.integrationtest.SecurityTestHelper.TestTargetType;
import com.daimler.sechub.integrationtest.internal.IntegrationTestContext;
import com.daimler.sechub.test.TestUtil;

class ServerEncryptionTest {

    private static SecurityTestHelper securityTestHelper;

    @BeforeAll
    static void beforeAll() throws Exception {
        IntegrationTestContext context = IntegrationTestContext.get();

        String checkAlive = context.getUrlBuilder().buildCheckIsAliveUrl();
        securityTestHelper = new SecurityTestHelper(TestTargetType.SECHUB_SERVER,new URL(checkAlive));
    }

    @Test
    void cipher_SSL_DHE_RSA_WITH_DES_CBC_SHA_is_not_accepted() throws Exception {
        Assume.assumeFalse("Currently not supported at Windows",TestUtil.isWindows());
        
        securityTestHelper.assertSSLCipherNotAccepted("SSL_DHE_RSA_WITH_DES_CBC_SHA");
    }
    
    @Test
    void wanted_ciphers_are_accepted() throws Exception {
        Assume.assumeFalse("Currently not supported at Windows", TestUtil.isWindows());
        
        securityTestHelper.assertOnlyAcceptedSSLCiphers(
                "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
                "TLS_AES_128_GCM_SHA256",
                "TLS_AES_256_GCM_SHA384",
                "TLS_AES_128_CCM_SHA256"
                );
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
