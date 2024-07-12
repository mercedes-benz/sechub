// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

import static org.junit.jupiter.api.Assertions.*;

import javax.crypto.SecretKey;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class DefaultSecretKeyProviderTest {

    @ParameterizedTest
    @NullSource
    @EmptySource
    void illegal_arguments_detected(String secret) {

        assertThrows(IllegalArgumentException.class, () -> new DefaultSecretKeyProvider(secret));
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 16, 32, 64 })
    void secretKey_in_bytes_is_correct_created(int secretKeyInBytes) {

        /* prepare */
        String testSecret = "x".repeat(secretKeyInBytes);

        /* execute */
        DefaultSecretKeyProvider providerToTest = new DefaultSecretKeyProvider(testSecret);

        /* test */
        assertEquals(secretKeyInBytes * 8, providerToTest.getLengthOfSecretInBits());

        SecretKey secretKey = providerToTest.getSecretKey();
        assertNotNull(secretKey);

        byte[] encoded = secretKey.getEncoded();
        String encodedString = new String(encoded);
        assertEquals(testSecret, encodedString);
    }

}
