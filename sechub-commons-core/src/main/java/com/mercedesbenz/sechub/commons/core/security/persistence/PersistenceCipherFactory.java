package com.mercedesbenz.sechub.commons.core.security.persistence;

import java.security.InvalidKeyException;

public class PersistenceCipherFactory {
    public static PersistenceCipher create(PersistenceCipherType cipherType, B64String secret) throws InvalidKeyException {
        PersistenceCipher cipher = null;

        switch (cipherType) {
        case NONE:
            cipher = NoneCipher.create(secret);
            break;
        case AES_256_GCM_SIV:
        case AES_128_GCM_SIV:
            cipher = AesGcmSiv.create(secret);
            break;
        default:
            cipher = AesGcmSiv.create(secret);
            break;
        }
        return cipher;
    }
}
