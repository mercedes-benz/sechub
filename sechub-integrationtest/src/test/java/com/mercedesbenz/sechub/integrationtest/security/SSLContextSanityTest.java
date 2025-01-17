// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.security;

import static com.mercedesbenz.sechub.integrationtest.SecurityTestHelper.*;

import javax.net.ssl.SSLContext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.integrationtest.api.TestOnlyForRegularExecution;

@TestOnlyForRegularExecution
class SSLContextSanityTest {

    @Test
    @DisplayName("Test JDK really fetches different instances for sane protocol")
    // "Test that JDK fetches different instances for ssl context, so it's clear we
    // do not change it globally inside our test")
    void sanity_check_SSLContext_getInstance_returns_always_new_objects() throws Exception {
        /* prepare */
        String protocol = TLS_V1_2;

        /* execute */
        SSLContext scA = SSLContext.getInstance(protocol);
        SSLContext scB = SSLContext.getInstance(protocol);

        /* test */
        Assertions.assertNotSame(scA, scB);
    }

}
