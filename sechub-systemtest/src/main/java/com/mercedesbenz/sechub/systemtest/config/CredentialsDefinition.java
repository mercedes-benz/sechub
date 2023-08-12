// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import javax.crypto.SealedObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

public class CredentialsDefinition extends AbstractDefinition {

    private static final CryptoAccess<String> cryptoAccess = new CryptoAccess<>();

    private String userId;

    @JsonIgnore
    SealedObject sealedApiToken;

    public String getApiToken() {
        return cryptoAccess.unseal(sealedApiToken);
    }

    public void setApiToken(String apiToken) {
        this.sealedApiToken = cryptoAccess.seal(apiToken);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
