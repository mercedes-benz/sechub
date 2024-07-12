// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.encryption.DefaultSecretKeyProvider;
import com.mercedesbenz.sechub.commons.encryption.SecretKeyProvider;

@Component
public class SecretKeyProviderFactory {

    public SecretKeyProvider createSecretKeyProvider(CipherPasswordSourceType passwordSourceType, String cipherPasswordSourceData) {

        switch (passwordSourceType) {
        case ENVIRONMENT_VARIABLE:
            String environmentVariableName = cipherPasswordSourceData;
            return new DefaultSecretKeyProvider(System.getenv(environmentVariableName));
        case NONE:
            return null; // here we need no secret key provider - none needs no password...
        default:
            throw new IllegalStateException("password type %s not supported!".formatted(passwordSourceType));
        }

    }

}
