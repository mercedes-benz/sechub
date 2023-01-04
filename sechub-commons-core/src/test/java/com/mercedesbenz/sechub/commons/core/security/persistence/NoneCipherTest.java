package com.mercedesbenz.sechub.commons.core.security.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.junit.jupiter.api.Test;

public class NoneCipherTest {
    @Test
    void encrypt_null_iv() throws InvalidKeyException, InvalidAlgorithmParameterException {
        String plainText = "This is a plaintext";
        
        PersistenceCipher cipher = NoneCipher.create(null);
        String cipherText = cipher.encrypt(plainText, null);
        
        assertEquals(plainText, cipherText);
    }
    
    @Test
    void encrypt_with_iv() throws InvalidKeyException, InvalidAlgorithmParameterException {
        String plainText = "This is a plaintext";
        
        PersistenceCipher cipher = NoneCipher.create(null);
        String cipherText = cipher.encrypt(plainText, "Hello");
        
        assertEquals(plainText, cipherText);
    }
    
    @Test
    void decrypt_null_iv() throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        String cipherText = "This is a cipher text";
        
        PersistenceCipher cipher = NoneCipher.create(null);
        String plainText = cipher.decrypt(cipherText, null);
        
        assertEquals(plainText, cipherText);
    }
    
    @Test
    void decrypt_with_iv() throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        String cipherText = "This is a cipher text";;
        
        PersistenceCipher cipher = NoneCipher.create(null);
        String plainText = cipher.decrypt(cipherText, "Hello");
        
        assertEquals(plainText, cipherText);
    }
    
    @Test
    void getCipher_null() throws InvalidKeyException {
        PersistenceCipher cipher = NoneCipher.create(null);
        assertEquals(PersistenceCipherType.NONE, cipher.getCipher());
    }
    
    @Test
    void getCipher_string() throws InvalidKeyException {
        PersistenceCipher cipher = NoneCipher.create("Hello");
        assertEquals(PersistenceCipherType.NONE, cipher.getCipher());
    }
}
