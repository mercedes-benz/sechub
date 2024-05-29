// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.properties;

import javax.crypto.SealedObject;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

public class SecretValidatorProxySettings {

    private String host;
    private int port;
    private SealedObject username;
    private SealedObject password;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return CryptoAccess.CRYPTO_STRING.unseal(username);
    }

    public void setUsername(String username) {
        this.username = CryptoAccess.CRYPTO_STRING.seal(username);
    }

    public void setPassword(String password) {
        this.password = CryptoAccess.CRYPTO_STRING.seal(password);
    }

    public String getPassword() {
        return CryptoAccess.CRYPTO_STRING.unseal(password);
    }

}
