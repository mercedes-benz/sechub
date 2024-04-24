package com.mercedesbenz.sechub.commons.core.security.persistence;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

public class RotationStrategy {
    private PersistenceCipher currentCipher;
    private PersistenceCipher newCipher;
    private boolean performSecretRotation = false;
    
    private RotationStrategy(PersistenceCipher currentCipher, PersistenceCipher newCipher, boolean performSecretRotation) {
        this.currentCipher = currentCipher;
        this.newCipher = newCipher;
        this.performSecretRotation = performSecretRotation;
    }
    
    public static RotationStrategy createSecretRotationStrategy(BinaryString currentSecret, BinaryString newSecret, PersistenceCipherType cipher) throws InvalidKeyException {
        PersistenceCipher currentCipher = PersistenceCipherFactory.create(cipher, currentSecret);
        PersistenceCipher newCipher = PersistenceCipherFactory.create(cipher, newSecret);
        
        boolean performSecretRotation = true;
        
        return new RotationStrategy(currentCipher, newCipher, performSecretRotation);
    }
    
  
    public static RotationStrategy createCipherAndSecretRotationStrategy(BinaryString currentSecret, BinaryString newSecret, PersistenceCipherType currentCipherType, PersistenceCipherType newCipherType) throws InvalidKeyException {
        PersistenceCipher currentCipher = PersistenceCipherFactory.create(currentCipherType, currentSecret);
        PersistenceCipher newCipher = PersistenceCipherFactory.create(newCipherType, newSecret);
        
        boolean performSecretRotation = true;
        
        return new RotationStrategy(currentCipher, newCipher, performSecretRotation);
    }
    
    public BinaryString rotate(BinaryString cipherText, BinaryString initializationVector) throws InvalidKeyException, IllegalArgumentException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        return rotate(cipherText, initializationVector, null, cipherText.getType());
    }
    
    public BinaryString rotate(BinaryString cipherText, BinaryString initializationVector, BinaryString newIntializationVector, BinaryStringEncodingType newBinaryStringEncoding) throws InvalidKeyException, IllegalArgumentException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        if (cipherText == null) {
            throw new IllegalArgumentException("The ciphertext cannot be null!");
        }
        
        if (initializationVector == null) {
            throw new IllegalArgumentException("The initialization vector (nonce) cannot be null!");
        }
        
        String plainText = currentCipher.decrypt(cipherText, initializationVector);
        BinaryString newCipherText = newCipher.encrypt(plainText, initializationVector);
        
        return newCipherText;
    }
    
    public PersistenceCipherType getCurrentCipher() {
        return currentCipher.getCipherType();
    }

    public PersistenceCipherType getNewCipher() {
        return newCipher.getCipherType();
    }
    
    public boolean isSecretRotationStrategy() {
        return performSecretRotation;
    }
    
    public boolean isCipherRotationStrategy() {
        return (currentCipher.getCipherType() != newCipher.getCipherType()) ? true : false;
    }
}
