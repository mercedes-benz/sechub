// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import javax.crypto.SealedObject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientCertificateConfiguration implements SecHubDataConfigurationUsageByName {
    public static final String PROPERTY_PASSWORD = "password";

    private CryptoAccess<char[]> cryptoAccess = CryptoAccess.CRYPTO_CHAR_ARRAY;

    private Optional<SealedObject> password = Optional.empty();
    private Set<String> namesOfUsedDataConfigurationObjects = new LinkedHashSet<>();

    @Override
    public Set<String> getNamesOfUsedDataConfigurationObjects() {
        return namesOfUsedDataConfigurationObjects;
    }

    public void setPassword(char[] password) {
        this.password = Optional.ofNullable(cryptoAccess.seal(password));
    }

    public char[] getPassword() {
        if (password.isEmpty()) {
            return null;
        }
        return cryptoAccess.unseal(password.get());
    }

}
