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
    public static PersistenceCipher create(String secret)  throws InvalidKeyException {
        return null;
    }
    
    public static String generateNewInitializationVector() {
        return null;
    }
    
    public String encrypt(String plaintext, String b64InitializationVector) throws InvalidAlgorithmParameterException, InvalidKeyException;

    public String decrypt(String b64Ciphertext, String b64InitializationVector)
            throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException;

    public PersistenceCipherType getCipher();
}
