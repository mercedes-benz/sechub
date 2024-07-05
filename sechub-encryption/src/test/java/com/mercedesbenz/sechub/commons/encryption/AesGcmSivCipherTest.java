// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

import static org.junit.jupiter.api.Assertions.*;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AesGcmSivCipherTest {

    private SecretKeySpec aes256secretKey;
    private SecretKeySpec aes128secretKey;

    @BeforeEach
    void beforeEach() {
        /* prepare */
        byte[] aes256pwd = "a".repeat(32).getBytes();
        aes256secretKey = new SecretKeySpec(aes256pwd, "AES");

        byte[] aes128pwd = "a".repeat(16).getBytes();
        aes128secretKey = new SecretKeySpec(aes128pwd, "AES");
    }

    @Test
    void aes_gcm_siv_256_encryption_and_deryption_works_in_general() {

        /* prepare */
        AesGcmSivCipher cipherToTest = new AesGcmSivCipher(aes256secretKey, PersistentCipherType.AES_GCM_SIV_256);

        InitializationVector initVector = cipherToTest.createNewInitializationVector();
        byte[] initVectorInBytes = initVector.getInitializationBytes();
        byte[] dataToEncrypt = "i am the plain text :-)".getBytes();

        /* execute */
        byte[] encryptedBytes = cipherToTest.encrypt(dataToEncrypt, initVector);

        /* test */
        AesGcmSivCipher cipherFromOutside = new AesGcmSivCipher(aes256secretKey, PersistentCipherType.AES_GCM_SIV_256);
        byte[] decrypted = cipherFromOutside.decrypt(encryptedBytes, new InitializationVector(initVectorInBytes));

        assertEquals(new String(dataToEncrypt), new String(decrypted));
        assertNotEquals(new String(encryptedBytes), new String(dataToEncrypt));

    }

    @Test
    void aes_gcm_siv_256_initialization_cipher_has_expected_length() {

        /* prepare */
        AesGcmSivCipher cipherToTest = new AesGcmSivCipher(aes256secretKey, PersistentCipherType.AES_GCM_SIV_256);

        /* execute */
        InitializationVector initVector = cipherToTest.createNewInitializationVector();

        /* test */
        assertEquals(AesGcmSivCipher.IV_LENGTH_IN_BYTES, initVector.getInitializationBytes().length);

    }

    @Test
    void aes_gcm_siv_128_initialization_cipher_has_expected_length() {

        /* prepare */
        AesGcmSivCipher cipherToTest = new AesGcmSivCipher(aes128secretKey, PersistentCipherType.AES_GCM_SIV_128);

        /* execute */
        InitializationVector initVector = cipherToTest.createNewInitializationVector();

        /* test */
        assertEquals(AesGcmSivCipher.IV_LENGTH_IN_BYTES, initVector.getInitializationBytes().length);

    }

    @Test
    void aes_gcm_siv_128_encryption_and_deryption_works_in_general() {

        /* prepare */
        AesGcmSivCipher cipherToTest = new AesGcmSivCipher(aes128secretKey, PersistentCipherType.AES_GCM_SIV_128);

        InitializationVector initVector = cipherToTest.createNewInitializationVector();
        byte[] initVectorInBytes = initVector.getInitializationBytes();
        byte[] dataToEncrypt = "i am the plain text :-)".getBytes();

        /* execute */
        byte[] encryptedBytes = cipherToTest.encrypt(dataToEncrypt, initVector);

        /* test */
        AesGcmSivCipher cipherFromOutside = new AesGcmSivCipher(aes128secretKey, PersistentCipherType.AES_GCM_SIV_128);
        byte[] decrypted = cipherFromOutside.decrypt(encryptedBytes, new InitializationVector(initVectorInBytes));

        assertEquals(new String(dataToEncrypt), new String(decrypted));
        assertNotEquals(new String(encryptedBytes), new String(dataToEncrypt));

    }

}
