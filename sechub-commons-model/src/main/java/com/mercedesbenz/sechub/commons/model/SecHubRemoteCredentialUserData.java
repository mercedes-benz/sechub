// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import javax.crypto.SealedObject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubRemoteCredentialUserData {

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_PASSWORD = "password";

    private SealedObject sealedName;

    private SealedObject sealedPassword;

    public String getName() {
        return CryptoAccess.CRYPTO_STRING.unseal(sealedName);
    }

    public void setName(String name) {
        this.sealedName = CryptoAccess.CRYPTO_STRING.seal(name);
    }

    public String getPassword() {
        return CryptoAccess.CRYPTO_STRING.unseal(sealedPassword);
    }

    public void setPassword(String password) {
        this.sealedPassword = CryptoAccess.CRYPTO_STRING.seal(password);
    }
}
