// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.encryption;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.encryption.DefaultSecretKeyProvider;
import com.mercedesbenz.sechub.commons.encryption.SecretKeyProvider;

@Component
public class SecHubSecretKeyProviderFactory {

    public SecretKeyProvider createSecretKeyProvider(SecHubCipherPasswordSourceType passwordSourceType, String cipherPasswordSourceData) {

        switch (passwordSourceType) {
        case ENVIRONMENT_VARIABLE:
            String environmentVariableName = cipherPasswordSourceData;
            return new DefaultSecretKeyProvider(System.getenv(environmentVariableName));
        case NONE:
            return null; // here we need no secret key provider - none needs no password...
        default:
            throw new IllegalStateException("Password source type '%s' is not supported!".formatted(passwordSourceType));
        }

    }

}
