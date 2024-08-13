// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class DefaultSecretKeyProviderTest {

    private static RandomGenerator randomGenerator;

    @BeforeAll
    static void beforeAll() {
        randomGenerator = RandomGeneratorFactory.getDefault().create();
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void illegal_arguments_detected(byte[] secret) {

        assertThrows(IllegalArgumentException.class, () -> new DefaultSecretKeyProvider(secret, PersistentCipherType.AES_GCM_SIV_128));
        assertThrows(IllegalArgumentException.class, () -> new DefaultSecretKeyProvider(secret, PersistentCipherType.AES_GCM_SIV_256));
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 16, 32, 64 })
    void secretKey_in_bytes_is_correct_created(int secretByteLength) {

        /* prepare */
        byte[] rawTestSecret = createSecretInBytes(secretByteLength);

        /* execute */
        DefaultSecretKeyProvider providerToTest = new DefaultSecretKeyProvider(rawTestSecret, PersistentCipherType.AES_GCM_SIV_256);

        /* test */
        assertEquals(secretByteLength * 8, providerToTest.getLengthOfSecretInBits());

        SecretKey secretKey = providerToTest.getSecretKey();
        assertNotNull(secretKey);

        byte[] encoded = secretKey.getEncoded();
        assertThat(encoded).isEqualTo(rawTestSecret);
    }

    private byte[] createSecretInBytes(int secretKeyInBytes) {
        byte[] randomSecret = new byte[secretKeyInBytes];
        randomGenerator.nextBytes(randomSecret);
        return randomSecret;
    }

}
