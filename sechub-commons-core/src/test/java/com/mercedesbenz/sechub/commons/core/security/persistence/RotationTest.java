package com.mercedesbenz.sechub.commons.core.security.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.junit.jupiter.api.Test;

public class RotationTest {
    @Test
    void secret_rotation_cipher_none() throws InvalidKeyException, IllegalArgumentException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        /* prepare */
        BinaryString currentSecret = new PlainString("abc");
        BinaryString newSecret = new PlainString("bca");
        BinaryString cipherText = new PlainString("hello");
        BinaryString initializationVector = new PlainString("iv");
        
        PersistenceCipherType cipherType = PersistenceCipherType.NONE;
        RotationStrategy rotation = RotationStrategy.createSecretRotationStrategy(currentSecret, newSecret, cipherType);
        
        /* execute */
        BinaryString newCipherText = rotation.rotate(cipherText, initializationVector);
        
        /* test */
        assertNotNull(rotation);
        assertEquals(cipherText, newCipherText);
        assertTrue(rotation.isSecretRotationStrategy());
        assertFalse(rotation.isCipherRotationStrategy());
        assertEquals(PersistenceCipherType.NONE, rotation.getCurrentCipher());
        assertEquals(PersistenceCipherType.NONE, rotation.getNewCipher());
    }
    
    @Test
    void secret_rotation_cipher_aes_gcm_siv_128() throws InvalidKeyException, IllegalArgumentException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        /* prepare */
        String expectedPlainText = "Hello, I am ☺️ 4 you.";
        BinaryString expectedCipherText = BinaryStringFactory.createFromBase64("8gWa4YPRlshBZCml8a0xvAJ1Y1mNU9iovclpvOhwVj4XiiaZvWKHWkU=", BinaryStringEncodingType.BASE64);
        BinaryString currentSecret = new PlainString("a".repeat(16));
        BinaryString newSecret = new PlainString("z".repeat(16));
        BinaryString cipherText = BinaryStringFactory.createFromBase64("DuwfqoAJrzZiK3u5v0XEnARPOjLugpobvWCxfTV6Y1FkAOECII/J8RU=", BinaryStringEncodingType.BASE64);
        BinaryString initializationVector = new PlainString("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));
        
        PersistenceCipherType cipherType = PersistenceCipherType.AES_GCM_SIV_128;
        RotationStrategy rotation = RotationStrategy.createSecretRotationStrategy(currentSecret, newSecret, cipherType);
        
        /* execute */
        BinaryString newCipherText = rotation.rotate(cipherText, initializationVector);
        
        /* test */
        assertNotNull(rotation);
        assertEquals(expectedCipherText, newCipherText);
        assertTrue(rotation.isSecretRotationStrategy());
        assertFalse(rotation.isCipherRotationStrategy());
        assertEquals(PersistenceCipherType.AES_GCM_SIV_128, rotation.getCurrentCipher());
        assertEquals(PersistenceCipherType.AES_GCM_SIV_128, rotation.getNewCipher());
        
        PersistenceCipher cipher = PersistenceCipherFactory.create(cipherType, newSecret);
        String plainText = cipher.decrypt(newCipherText, initializationVector);
        
        assertEquals(expectedPlainText, plainText);
    }
    
    @Test
    void cipher_and_secret_rotation_cipher_none_to_cipher_aes_gcm_siv_128() throws InvalidKeyException, IllegalArgumentException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        /* prepare */
        String expectedPlainText = "Hello, I am ☺️ 4 you.";
        BinaryString expectedCipherText = BinaryStringFactory.createFromBase64("DuwfqoAJrzZiK3u5v0XEnARPOjLugpobvWCxfTV6Y1FkAOECII/J8RU=", BinaryStringEncodingType.BASE64);
        BinaryString currentSecret = new PlainString("abc");
        BinaryString newSecret = new PlainString("a".repeat(16));
        BinaryString cipherText = new PlainString(expectedPlainText);
        BinaryString initializationVector = new PlainString("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));
        
        PersistenceCipherType cipherType = PersistenceCipherType.NONE;
        RotationStrategy rotation = RotationStrategy.createSecretRotationStrategy(currentSecret, newSecret, cipherType);
        
        /* execute */
        BinaryString newCipherText = rotation.rotate(cipherText, initializationVector);
        
        /* test */
        assertNotNull(rotation);
        assertEquals(cipherText, newCipherText);
        assertTrue(rotation.isSecretRotationStrategy());
        assertFalse(rotation.isCipherRotationStrategy());
        assertEquals(PersistenceCipherType.NONE, rotation.getCurrentCipher());
        assertEquals(PersistenceCipherType.NONE, rotation.getNewCipher());
    }
    
//    @Test
//    void from_cipher_rotation() throws IllegalAccessException {
//        String currentKey = "abc";
//        String newKey = "a".repeat(16);
//        RotationStrategy rotation = RotationStrategy.createCipherRotationStrategy(PersistenceCipherType.NONE, PersistenceCipherType.AES_128_GCM_SIV, currentKey, newKey);
//        
//        assertNotNull(rotation);
//        assertFalse(rotation.isSecretRotationStrategy());
//        assertTrue(rotation.isCipherRotationStrategy());
//        assertEquals(rotation.getCurrentCipher(), PersistenceCipherType.NONE);
//        assertEquals(rotation.getNewCipher(), PersistenceCipherType.AES_128_GCM_SIV);
//    }
    
}
