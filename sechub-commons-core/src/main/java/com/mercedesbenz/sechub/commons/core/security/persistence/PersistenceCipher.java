package com.mercedesbenz.sechub.commons.core.security.persistence;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

/**
 * Interface for crypotgraphic algorithms used to protect data at rest (persistence layer)
 * 
 * @author Jeremias Eppler
 */
public interface PersistenceCipher {
    public static PersistenceCipher create(B64String secret)  throws InvalidKeyException {
        return null;
    }
    
    public static String generateNewInitializationVector() {
        return null;
    }
    
    public B64String encrypt(String plaintext, B64String initializationVector) throws InvalidAlgorithmParameterException, InvalidKeyException;

    public String decrypt(B64String b64Ciphertext, B64String initializationVector)
            throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException;

    public PersistenceCipherType getCipher();
}
