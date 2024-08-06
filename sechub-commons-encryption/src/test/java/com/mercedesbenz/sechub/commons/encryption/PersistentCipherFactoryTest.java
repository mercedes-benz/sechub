// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PersistentCipherFactoryTest {

    private PersistentCipherFactory factoryToTest;

    @BeforeEach
    void beforeEach() {
        factoryToTest = new PersistentCipherFactory();
    }

    @Test
    void type_aes_gcm_siv_256_can_be_created__with_secret_of_32Byte() {
        /* prepare */
        SecretKeyProvider secretKeyProvider = mock(SecretKeyProvider.class);
        when(secretKeyProvider.getLengthOfSecretInBits()).thenReturn(256);

        /* execute */
        PersistentCipher cipher = factoryToTest.createCipher(secretKeyProvider, PersistentCipherType.AES_GCM_SIV_256);

        /* test */
        assertTrue(cipher instanceof AesGcmSivCipher);
        assertEquals(PersistentCipherType.AES_GCM_SIV_256, cipher.getType());

    }

    @Test
    void type_aes_gcm_siv_1289_can_be_created__with_secret_of128bit() {
        /* prepare */
        SecretKeyProvider secretKeyProvider = mock(SecretKeyProvider.class);
        when(secretKeyProvider.getLengthOfSecretInBits()).thenReturn(128);

        /* execute */
        PersistentCipher cipher = factoryToTest.createCipher(secretKeyProvider, PersistentCipherType.AES_GCM_SIV_128);

        /* test */
        assertTrue(cipher instanceof AesGcmSivCipher);
        assertEquals(PersistentCipherType.AES_GCM_SIV_128, cipher.getType());

    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 16, 128, 512 })
    void type_aes_gcm_siv_256_can_NOT_be_created__with_secret_having_wrong_amunt_of_bits(int amountOfBits) {
        /* prepare */
        SecretKeyProvider secretKeyProvider = mock(SecretKeyProvider.class);
        when(secretKeyProvider.getLengthOfSecretInBits()).thenReturn(amountOfBits);

        /* execute */
        assertThrows(IllegalArgumentException.class, () -> factoryToTest.createCipher(secretKeyProvider, PersistentCipherType.AES_GCM_SIV_256));

    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 16, 64, 256 })
    void type_aes_gcm_siv_128_can_NOT_be_created__with_secret_having_wrong_amunt_of_bits(int amountOfBits) {

        /* prepare */
        SecretKeyProvider secretKeyProvider = mock(SecretKeyProvider.class);
        when(secretKeyProvider.getLengthOfSecretInBits()).thenReturn(amountOfBits);

        /* execute */
        assertThrows(IllegalArgumentException.class, () -> factoryToTest.createCipher(secretKeyProvider, PersistentCipherType.AES_GCM_SIV_128));

    }

    @Test
    void type_none_can_be_created() {

        /* execute */
        PersistentCipher cipher = factoryToTest.createCipher(null, PersistentCipherType.NONE);

        /* test */
        assertTrue(cipher instanceof NoneCipher);
        assertEquals(PersistentCipherType.NONE, cipher.getType());

    }

}
