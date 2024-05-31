// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.security.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.security.InvalidKeyException;

import org.junit.jupiter.api.Test;

public class PersistenceCipherFactoryTest {
    @Test
    void create_none_cipher() throws InvalidKeyException {
        /* prepare */
        PersistenceCipherType cipherType = PersistenceCipherType.NONE;
        Base64String secret = new Base64String("topSecret");

        /* execute */
        PersistenceCipher cipher = PersistenceCipherFactory.create(cipherType, secret);

        /* test */
        assertEquals(cipher.getCipherType(), cipherType);
    }

    @Test
    void create_aes_gcm_siv_256() throws InvalidKeyException {
        /* prepare */
        PersistenceCipherType cipherType = PersistenceCipherType.AES_GCM_SIV_256;
        BinaryString secret = new PlainString("a".repeat(32));

        /* execute */
        PersistenceCipher cipher = PersistenceCipherFactory.create(cipherType, secret);

        /* test */
        assertEquals(cipher.getCipherType(), cipherType);
    }

    @Test
    void create_aes_gcm_siv_128() throws InvalidKeyException {
        /* prepare */
        PersistenceCipherType cipherType = PersistenceCipherType.AES_GCM_SIV_128;
        BinaryString secret = new HexString("a".repeat(16));

        /* execute */
        PersistenceCipher cipher = PersistenceCipherFactory.create(cipherType, secret);

        /* test */
        assertEquals(cipher.getCipherType(), cipherType);
    }

    @Test
    void create_initializationVector_aes_gcm_siv_256() throws InvalidKeyException {
        /* prepare */
        PersistenceCipherType cipherType = PersistenceCipherType.AES_GCM_SIV_256;
        BinaryString secret = new PlainString("a".repeat(32));
        PersistenceCipher cipher = PersistenceCipherFactory.create(cipherType, secret);

        /* execute */
        BinaryString initializationVector = cipher.generateNewInitializationVector();

        /* test */
        assertEquals(cipher.getCipherType(), cipherType);
        assertNotNull(initializationVector);
        assertEquals(AesGcmSiv.IV_LENGTH_IN_BYTES, initializationVector.getBytes().length);
    }

    @Test
    void create_initializationVector_aes_gcm_siv_128() throws InvalidKeyException {
        /* prepare */
        PersistenceCipherType cipherType = PersistenceCipherType.AES_GCM_SIV_128;
        BinaryString secret = new PlainString("a".repeat(16));
        PersistenceCipher cipher = PersistenceCipherFactory.create(cipherType, secret);

        /* execute */
        BinaryString initializationVector = cipher.generateNewInitializationVector();

        /* test */
        assertEquals(cipher.getCipherType(), cipherType);
        assertNotNull(initializationVector);
        assertEquals(AesGcmSiv.IV_LENGTH_IN_BYTES, initializationVector.getBytes().length);
    }

    @Test
    void create_initializationVector_none_cipher() throws InvalidKeyException {
        /* prepare */
        PersistenceCipherType cipherType = PersistenceCipherType.NONE;
        BinaryString secret = new PlainString("a".repeat(3));
        PersistenceCipher cipher = PersistenceCipherFactory.create(cipherType, secret);

        /* execute */
        BinaryString initializationVector = cipher.generateNewInitializationVector();

        /* test */
        assertEquals(cipher.getCipherType(), cipherType);
        assertNotNull(initializationVector);
        assertEquals("", initializationVector.toString());
    }
}
