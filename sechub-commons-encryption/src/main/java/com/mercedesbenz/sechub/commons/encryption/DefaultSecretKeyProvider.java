// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Default implementation of a secret key provider. Uses AES for key encryption
 * and UTF 8 charset encoding
 *
 * @author Albert Tregnaghi
 *
 */
public class DefaultSecretKeyProvider implements SecretKeyProvider {

    private int lengthInBits;
    private SecretKey secretKey;

    public DefaultSecretKeyProvider(String secret) {
        if (secret == null) {
            throw new IllegalArgumentException("secret may not be null");
        }
        if (secret.isEmpty()) {
            throw new IllegalArgumentException("secret may not be null");
        }
        byte[] rawSecret = secret.getBytes(EncryptionConstants.UTF8_CHARSET_ENCODING);
        this.lengthInBits = rawSecret.length * 8;

        secretKey = new SecretKeySpec(rawSecret, 0, rawSecret.length, "AES");
    }

    @Override
    public SecretKey getSecretKey() {
        return secretKey;
    }

    @Override
    public int getLengthOfSecretInBits() {
        return lengthInBits;
    }

}
