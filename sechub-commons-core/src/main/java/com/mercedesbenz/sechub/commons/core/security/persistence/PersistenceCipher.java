package com.mercedesbenz.sechub.commons.core.security.persistence;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

/**
 * Interface for cryptographic algorithms used to protect data at rest (persistence layer)
 * 
 * @author Jeremias Eppler
 */
public interface PersistenceCipher {
    public static PersistenceCipher create(BinaryString secret)  throws InvalidKeyException {
        return null;
    }
    
    public static BinaryString generateNewInitializationVector() {
        return null;
    }
    
    public BinaryString encrypt(String plaintext, BinaryString initializationVector) throws InvalidAlgorithmParameterException, InvalidKeyException;
    
    public BinaryString encrypt(String plaintext, BinaryString initializationVector, BinaryStringEncodingType encodingType) throws InvalidAlgorithmParameterException, InvalidKeyException;

    public String decrypt(BinaryString ciphertext, BinaryString initializationVector)
            throws IllegalArgumentException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException;

    public PersistenceCipherType getCipherType();
}
