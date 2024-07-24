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

    public DefaultSecretKeyProvider(byte[] rawSecret, PersistentCipherType cipherType) {
        if (rawSecret == null) {
            throw new IllegalArgumentException("secret may not be null");
        }
        if (rawSecret.length == 0) {
            throw new IllegalArgumentException("secret bytes array may not be empty");
        }
        if (cipherType == null) {
            throw new IllegalArgumentException("cipher type not defined");
        }
        String secretKeyAlgorithm = cipherType.getSecretKeyAlgorithm();
        if (secretKeyAlgorithm == null || secretKeyAlgorithm.isBlank()) {
            throw new IllegalArgumentException("cipher type: " + cipherType.getClass().getSimpleName() + " does not provide an algorithm for secret keys!");
        }

        lengthInBits = rawSecret.length * 8;

        secretKey = new SecretKeySpec(rawSecret, 0, rawSecret.length, secretKeyAlgorithm);
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
