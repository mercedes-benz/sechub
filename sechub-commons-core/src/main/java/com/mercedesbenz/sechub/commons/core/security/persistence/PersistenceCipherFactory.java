package com.mercedesbenz.sechub.commons.core.security.persistence;

import java.security.InvalidKeyException;

public class PersistenceCipherFactory {
    public static PersistenceCipher create(PersistenceCipherType cipherType, String seceret) throws InvalidKeyException {
        PersistenceCipher cipher = null;

        switch (cipherType) {
        case NONE:
            cipher = NoneCipher.create(seceret);
            break;
        case AES_256_GCM_SIV:
        case AES_128_GCM_SIV:
            cipher = AesGcmSiv.create(seceret);
            break;
        default:
            cipher = AesGcmSiv.create(seceret);
            break;
        }
        return cipher;
    }
}
