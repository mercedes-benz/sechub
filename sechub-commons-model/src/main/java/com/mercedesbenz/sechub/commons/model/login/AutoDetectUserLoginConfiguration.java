// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.login;

import javax.crypto.SealedObject;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

public class AutoDetectUserLoginConfiguration {

    private CryptoAccess<char[]> cryptoAccess = CryptoAccess.CRYPTO_CHAR_ARRAY;
    private char[] user;
    SealedObject password;

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