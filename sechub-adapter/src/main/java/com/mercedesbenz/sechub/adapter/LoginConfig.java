// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import java.net.URL;

import javax.crypto.SealedObject;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

public interface LoginConfig {

    default boolean isBasic() {
        return this instanceof BasicLoginConfig;
    }

    default boolean isFormAutoDetect() {
        return this instanceof FormAutoDetectLoginConfig;
    }

    default boolean isFormScript() {
        return this instanceof FormScriptLoginConfig;
    }

    public URL getLoginURL();

    default BasicLoginConfig asBasic() {
        return (BasicLoginConfig) this;
    }

    default FormScriptLoginConfig asFormScript() {
        return (FormScriptLoginConfig) this;
    }

    default String decrypt(SealedObject sealed) {
        return CryptoAccess.CRYPTO_STRING.unseal(sealed);
    }

    default SealedObject encrypt(String string) {
        return CryptoAccess.CRYPTO_STRING.seal(string);
    }
}
