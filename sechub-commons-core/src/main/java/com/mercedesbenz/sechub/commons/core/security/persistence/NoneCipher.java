package com.mercedesbenz.sechub.commons.core.security.persistence;

import java.security.InvalidKeyException;

/**
 * A cipher which does not encrypt anything.
 * 
 * It encodes and decodes the string to and from Base64.
 * 
 * @author Jeremias Eppler
 */
public class NoneCipher implements PersistenceCipher {
    private NoneCipher() {}

    public B64String encrypt(String plaintext, B64String initialzationVector) {
        return B64String.from(plaintext);
    }

    public String decrypt(B64String ciphertext, B64String initialzationVector) {
        return ciphertext.getString();
    }

    @Override
    public PersistenceCipherType getCipher() {
        return PersistenceCipherType.NONE;
    }

    public static PersistenceCipher create(B64String b64Secret)  throws InvalidKeyException {
        return new NoneCipher();
    }

}
