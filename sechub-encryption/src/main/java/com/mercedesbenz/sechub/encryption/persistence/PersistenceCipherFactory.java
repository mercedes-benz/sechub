// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.encryption.persistence;

import java.security.InvalidKeyException;

/**
 * The factory creates persistent ciphers used to protect data at rest.
 *
 * The algorithms protect both the confidentiality and integrity of the
 * information at rest.
 *
 * In addition, the ciphers which it creates are nonce (initialization vector)
 * misuse-resistant.
 *
 * @author Jeremias Eppler
 */
public class PersistenceCipherFactory {

    /**
     * Creates a new persistent cipher based on the type and secret.
     *
     * @param cipherType
     * @param secret
     * @return
     * @throws InvalidKeyException
     */
    public static PersistenceCipher create(PersistenceCipherType cipherType, BinaryString secret) throws InvalidKeyException {
        PersistenceCipher cipher = null;

        switch (cipherType) {
        case NONE:
            cipher = NoneCipher.create(secret);
            break;
        case AES_GCM_SIV_256:
        case AES_GCM_SIV_128:
            cipher = AesGcmSiv.create(secret);
            break;
        default:
            throw new IllegalArgumentException("Unable to create cipher. Unknown cipher.");
        }

        return cipher;
    }
}
