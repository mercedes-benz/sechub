// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.encryption;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.encryption.DefaultSecretKeyProvider;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipherType;
import com.mercedesbenz.sechub.commons.encryption.SecretKeyProvider;

@Component
public class SecHubSecretKeyProviderFactory {

    @Autowired
    EncryptionEnvironmentEntryProvider encryptionEnvironmentEntryProvider;

    public SecretKeyProvider createSecretKeyProvider(PersistentCipherType cipherType, SecHubCipherPasswordSourceType passwordSourceType,
            String cipherPasswordSourceData) {

        switch (passwordSourceType) {
        case ENVIRONMENT_VARIABLE:
            String environmentVariableName = cipherPasswordSourceData;
            String environmentEntry = encryptionEnvironmentEntryProvider.getBase64EncodedEnvironmentEntry(environmentVariableName);

            byte[] base64decoded = Base64.getDecoder().decode(environmentEntry);

            return new DefaultSecretKeyProvider(base64decoded, cipherType);

        case NONE:
            return null; // here we need no secret key provider - none needs no password...
        default:
            throw new IllegalStateException("Password source type '%s' is not supported!".formatted(passwordSourceType));
        }

    }

}
