package com.daimler.sechub.commons.core.security;

public class PersistenceNoneCipher {
    private PersistenceNoneCipher() {
    }
    
    public static PersistenceNoneCipher init(String secret) {
        return new PersistenceNoneCipher();
    }
    
    public String encrypt(String plaintext, String initialzationVector) {
        return plaintext;
    }

    public String decrypt(String plaintext, String initialzationVector) {
        return plaintext;
    }

}
