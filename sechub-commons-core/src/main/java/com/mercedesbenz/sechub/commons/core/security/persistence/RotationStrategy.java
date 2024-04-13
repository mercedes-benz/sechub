package com.mercedesbenz.sechub.commons.core.security.persistence;

public class RotationStrategy {
    private PersistenceCipherType currentCipher;
    private PersistenceCipherType newCipher;
    private BinaryString currentSecret;
    private BinaryString newSecret;
    
    private RotationStrategy(PersistenceCipherType currentCipher, PersistenceCipherType newCipher, BinaryString currentSecret, BinaryString newSecret) {

        this.currentCipher = currentCipher;
        this.newCipher = newCipher;
        this.currentSecret = currentSecret;
        this.newSecret = newSecret;
        
        if (newCipher == null) {
            this.newCipher = currentCipher;
        } else {
            this.newCipher = newCipher;
        }
        
    }
    
    public static RotationStrategy createSecretRotationStrategy(PersistenceCipherType cipher, BinaryString currentSecret, BinaryString newSecret) {
        return new RotationStrategy(cipher, null, currentSecret, newSecret);
    }
    
//    public static RotationStrategy createCipherRotationStrategy(PersistenceCipherType currentCipher, PersistenceCipherType newCipher, String currentSecret) {
//        return new RotationStrategy(currentCipher, newCipher, currentSecret, newSecret);
//    }
//    
//    public static RotationStrategy createCipherAndSecretRotationStrategy(PersistenceCipherType currentCipher, PersistenceCipherType newCipher, String currentSecret, String newSecret) {
//        return new RotationStrategy(currentCipher, newCipher, currentSecret, newSecret);
//    }
    
    public BinaryString rotate(BinaryString cipherText, BinaryString initializationVector) {
        return rotate(cipherText, initializationVector, cipherText.getType());
    }
    
    public BinaryString rotate(BinaryString cipherText, BinaryString initializationVector, BinaryStringEncodingType newBinaryStringEncoding) {
        return null;
    }
    
    public PersistenceCipherType getCurrentCipher() {
        return currentCipher;
    }

    public PersistenceCipherType getNewCipher() {
        return newCipher;
    }
    
    public boolean isSecretRotationStrategy() {
        return (newCipher == currentCipher) ? true : false;
    }
    
    public boolean isCipherRotationStrategy() {
        return (currentCipher != newCipher) ? true : false;
    }
}
