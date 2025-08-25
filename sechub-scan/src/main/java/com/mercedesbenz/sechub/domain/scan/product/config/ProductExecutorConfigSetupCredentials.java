// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.config;

import javax.crypto.SealedObject;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

public class ProductExecutorConfigSetupCredentials {

    public static final String PROPERTY_USER = "user";
    public static final String PROPERTY_PASSWORD = "password";

    private CryptoAccess<String> cryptoAccess = CryptoAccess.CRYPTO_STRING;

    private SealedObject user;

    private SealedObject password;

    public void setUser(String user) {
        this.user = cryptoAccess.seal(user);
    }

    public String getUser() {
        return cryptoAccess.unseal(user);
    }

    public void setPassword(String password) {
        this.password = cryptoAccess.seal(password);
    }

    public String getPassword() {
        return cryptoAccess.unseal(password);
    }
}
