// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccessProvider;

@Component
public class OAuth2OpaqueTokenIntrospectionResponseCryptoAccessProvider implements CryptoAccessProvider<OAuth2OpaqueTokenIntrospectionResponse> {

    private CryptoAccess<OAuth2OpaqueTokenIntrospectionResponse> cryptoAccess = new CryptoAccess<>();

    @Override
    public CryptoAccess<OAuth2OpaqueTokenIntrospectionResponse> getCryptoAccess() {
        return cryptoAccess;
    }

}
