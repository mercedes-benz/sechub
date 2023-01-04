package com.mercedesbenz.sechub.commons.core.security.persistence;

import java.security.InvalidKeyException;

/**
 * A cipher which does not encrypt anything.
 * 
 * @author Jeremias Eppler
 */
public class NoneCipher implements PersistenceCipher {
    private NoneCipher() {}

    public String encrypt(String plaintext, String initialzationVector) {
        return plaintext;
    }

    public String decrypt(String plaintext, String initialzationVector) {
        return plaintext;
    }

    @Override
    public PersistenceCipherType getCipher() {
        return PersistenceCipherType.NONE;
    }

    public static PersistenceCipher create(String secret)  throws InvalidKeyException {
        return new NoneCipher();
    }

}
