package com.mercedesbenz.sechub.commons.core.security.persistence;

import java.security.InvalidKeyException;

public class PersistenceCipherFactory {
    public static PersistenceCipher create(PersistenceCipherType cipherType, BinaryString secret) throws InvalidKeyException {
        PersistenceCipher cipher = null;

        switch (cipherType) {
        case NONE:
            cipher = NoneCipher.create(secret);
            break;
        case AES_GCM_SIV_256:
        case AES_GCM_SIV_192:
        case AES_GCM_SIV_128:
            cipher = AesGcmSiv.create(secret);
            break;
        default:
            throw new IllegalArgumentException("Unable to create cipher. Unknown cipher.");
        }
        
        return cipher;
    }
}
