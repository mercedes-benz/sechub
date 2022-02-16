// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.model.login;

import java.util.Optional;

import javax.crypto.SealedObject;

import com.daimler.sechub.commons.core.security.CryptoAccess;

public class BasicLoginConfiguration {
    private CryptoAccess<char[]> cryptoAccess = CryptoAccess.CRYPTO_CHAR_ARRAY;

    private Optional<String> realm;
    private char[] user;
    SealedObject password;

    public Optional<String> getRealm() {
        return realm;
    }

    public void setUser(char[] user) {
        this.user = user;
    }

    public char[] getUser() {
        return user;
    }

    public void setPassword(char[] password) {
        this.password = cryptoAccess.seal(password);
    }

    public char[] getPassword() {
        return cryptoAccess.unseal(password);
    }

}