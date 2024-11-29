// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static java.util.Objects.requireNonNull;

import java.nio.charset.StandardCharsets;

import javax.crypto.SealedObject;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

@ConfigurationProperties(prefix = AES256EncryptionProperties.PREFIX)
class AES256EncryptionProperties {

    static final String PREFIX = "sechub.security.encryption";
    private static final String ERR_MSG_FORMAT = "The property '%s.%s' must not be null";
    private static final int AES_256_SECRET_KEY_LENGTH = 32;

    private final SealedObject secretKeySealed;

    @ConstructorBinding
    AES256EncryptionProperties(String secretKey) {
        requireNonNull(secretKey, ERR_MSG_FORMAT.formatted(PREFIX, "secret-key"));
        this.secretKeySealed = CryptoAccess.CRYPTO_STRING.seal(secretKey);
        if (!is256BitString(secretKey)) {
            throw new IllegalArgumentException("The property %s.%s must be a 256-bit string".formatted(PREFIX, "secret-key"));
        }
    }

    byte[] getSecretKeyBytes() {
        return CryptoAccess.CRYPTO_STRING.unseal(secretKeySealed).getBytes(StandardCharsets.UTF_8);
    }

    /*
     * Checks if the secret key length is 32 characters (32 * 8 = 256 bits)
     */
    private static boolean is256BitString(String secretKey) {
        return secretKey.length() == AES_256_SECRET_KEY_LENGTH;
    }
}
