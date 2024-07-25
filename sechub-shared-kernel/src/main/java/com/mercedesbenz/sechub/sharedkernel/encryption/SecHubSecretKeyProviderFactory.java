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
        if (PersistentCipherType.NONE.equals(cipherType)) {
            /* none has never a secret key provider - there is just no secret */
            return null;
        }
        try {
            return handle(cipherType, passwordSourceType, cipherPasswordSourceData);

        } catch (Exception e) {
            throw new SecHubSecretKeyProviderFactoryException(
                    "Was not able to create key provider for cipherType: '%s', passwordSourceType: '%s', cipherPasswordSourceData: '%s'".formatted(cipherType,
                            passwordSourceType, cipherPasswordSourceData),
                    e);
        }

    }

    private SecretKeyProvider handle(PersistentCipherType cipherType, SecHubCipherPasswordSourceType passwordSourceType, String cipherPasswordSourceData) {
        switch (passwordSourceType) {

        case ENVIRONMENT_VARIABLE:
            String environmentVariableName = cipherPasswordSourceData;
            String environmentEntry = encryptionEnvironmentEntryProvider.getBase64EncodedEnvironmentEntry(environmentVariableName);
            if (environmentEntry == null || environmentEntry.isBlank()) {
                throw new IllegalArgumentException("The environment variable: " + environmentVariableName + " has no value!");
            }
            byte[] base64decoded = Base64.getDecoder().decode(environmentEntry);

            return new DefaultSecretKeyProvider(base64decoded, cipherType);

        default:
            throw new IllegalArgumentException("Password source type '%s' for cipher type: '%s' is not supported!".formatted(passwordSourceType, cipherType));
        }
    }
}
