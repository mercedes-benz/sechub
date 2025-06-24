// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OAuth2OpaqueTokenIntrospectionResponseCryptoAccessProviderTest {

    private OAuth2OpaqueTokenIntrospectionResponseCryptoAccessProvider providerToTest;

    @BeforeEach
    void beforeEach() {
        providerToTest = new OAuth2OpaqueTokenIntrospectionResponseCryptoAccessProvider();
    }

    @Test
    void returned_crypto_access_is_not_null() {
        assertThat(providerToTest.getCryptoAccess()).isNotNull();
    }

}
