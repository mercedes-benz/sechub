package com.mercedesbenz.sechub.commons.core.security.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.junit.jupiter.api.Test;

public class NoneCipherTest {
    private BinaryString initializationVector = BinaryStringFactory.createFromString("Hello");
    @Test
    void encrypt_null_iv() throws InvalidKeyException, InvalidAlgorithmParameterException {
        String plaintext = "This is plaintext";
        
        PersistenceCipher cipher = NoneCipher.create(null);
        BinaryString ciphertext = cipher.encrypt(plaintext, null);
        
        assertEquals(plaintext, ciphertext.toString());
    }
    
    @Test
    void encrypt_with_iv() throws InvalidKeyException, InvalidAlgorithmParameterException {
        String plaintext = "This is plaintext";
        
        PersistenceCipher cipher = NoneCipher.create(null);
        BinaryString ciphertext = cipher.encrypt(plaintext, initializationVector);
        
        assertEquals(plaintext, ciphertext.toString());
    }
    
    @Test
    void decrypt_null_iv() throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        String expectedPlaintext = "This is plaintext";
        BinaryString ciphertext = new PlainString(expectedPlaintext);
        
        PersistenceCipher cipher = NoneCipher.create(null);
        String plaintext = cipher.decrypt(ciphertext, null);
        
        assertEquals(expectedPlaintext, plaintext);
    }
    
    @Test
    void decrypt_with_iv() throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        String expectedPlaintext = "This is plaintext with a 🐎!";
        BinaryString ciphertext = new PlainString(expectedPlaintext);
        
        PersistenceCipher cipher = NoneCipher.create(null);
        String plaintext = cipher.decrypt(ciphertext, initializationVector);
        
        assertEquals(expectedPlaintext, plaintext);
    }
    
    @Test
    void getCipher_null() throws InvalidKeyException {
        PersistenceCipher cipher = NoneCipher.create(null);
        assertEquals(PersistenceCipherType.NONE, cipher.getCipherType());
    }
    
    @Test
    void getCipher_string() throws InvalidKeyException {
        PersistenceCipher cipher = NoneCipher.create(new Base64String("Hello"));
        assertEquals(PersistenceCipherType.NONE, cipher.getCipherType());
    }
}
