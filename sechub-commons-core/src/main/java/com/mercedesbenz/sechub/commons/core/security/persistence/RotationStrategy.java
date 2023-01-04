package com.mercedesbenz.sechub.commons.core.security.persistence;

public class RotationStrategy {
    private PersistenceCipherType currentCipher;
    private PersistenceCipherType newCipher;
    private String currentSecret;
    private String newSecret;
    
    private RotationStrategy(PersistenceCipherType currentCipher, PersistenceCipherType newCipher, String currentSecret, String newSecret) {

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
    
    public static RotationStrategy createSecretRotationStrategy(PersistenceCipherType cipher, String currentSecret, String newSecret) {
        return new RotationStrategy(cipher, null, currentSecret, newSecret);
    }
    
    public static RotationStrategy createCipherRotationStrategy(PersistenceCipherType currentCipher, PersistenceCipherType newCipher, String currentSecret, String newSecret) {
        return new RotationStrategy(currentCipher, newCipher, currentSecret, newSecret);
    }
    
    public String rotate(String plainText) {
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
