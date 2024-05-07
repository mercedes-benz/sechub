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
        /* prepare */
        String plaintext = "This is plaintext";
        PersistenceCipher cipher = NoneCipher.create(null);

        /* execute */
        BinaryString ciphertext = cipher.encrypt(plaintext, null);

        /* test */
        assertEquals(plaintext, ciphertext.toString());
    }

    @Test
    void encrypt_with_iv() throws InvalidKeyException, InvalidAlgorithmParameterException {
        /* prepare */
        String plaintext = "This is plaintext";
        PersistenceCipher cipher = NoneCipher.create(null);

        /* execute */
        BinaryString ciphertext = cipher.encrypt(plaintext, initializationVector);

        /* test */
        assertEquals(plaintext, ciphertext.toString());
    }

    @Test
    void decrypt_null_iv() throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        /* prepare */
        String expectedPlaintext = "This is plaintext";
        BinaryString ciphertext = new PlainString(expectedPlaintext);
        PersistenceCipher cipher = NoneCipher.create(null);

        /* execute */
        String plaintext = cipher.decrypt(ciphertext, null);

        /* test */
        assertEquals(expectedPlaintext, plaintext);
    }

    @Test
    void decrypt_with_iv() throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        /* prepare */
        String expectedPlaintext = "This is plaintext with a 🐎!";
        BinaryString ciphertext = new PlainString(expectedPlaintext);
        PersistenceCipher cipher = NoneCipher.create(null);

        /* execute */
        String plaintext = cipher.decrypt(ciphertext, initializationVector);

        /* test */
        assertEquals(expectedPlaintext, plaintext);
    }

    @Test
    void getCipher_null() throws InvalidKeyException {
        /* prepare */
        PersistenceCipher cipher = NoneCipher.create(null);

        /* execute + test */
        assertEquals(PersistenceCipherType.NONE, cipher.getCipherType());
    }

    @Test
    void getCipher_string() throws InvalidKeyException {
        /* prepare */
        PersistenceCipher cipher = NoneCipher.create(new Base64String("Hello"));

        /* execute + test */
        assertEquals(PersistenceCipherType.NONE, cipher.getCipherType());
    }

    @Test
    void generateNewInitializationVector_default_type() throws InvalidKeyException {
        /* prepare */
        PersistenceCipher cipher = NoneCipher.create(new Base64String("Hello"));

        /* execute */
        BinaryString initializationVector = cipher.generateNewInitializationVector();

        /* test */
        assertEquals(PersistenceCipherType.NONE, cipher.getCipherType());
        assertEquals(BinaryStringEncodingType.PLAIN, initializationVector.getType());
        assertEquals("", initializationVector.toString());
    }

    @Test
    void generateNewInitializationVector_type_provided() throws InvalidKeyException {
        /* prepare */
        PersistenceCipher cipher = NoneCipher.create(new Base64String("Hello"));

        /* execute */
        BinaryString initializationVector = cipher.generateNewInitializationVector(BinaryStringEncodingType.HEX);

        /* test */
        assertEquals(PersistenceCipherType.NONE, cipher.getCipherType());
        assertEquals(BinaryStringEncodingType.HEX, initializationVector.getType());
        assertEquals("", initializationVector.toString());
    }
}
