// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.encryption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.GeneralSecurityException;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

class AES256EncryptionTest {

    private static final String VALID_AES_256_TEST_SECRET_KEY = "test-test-test-test-test-test-32";
    private static final AES256EncryptionProperties aes256EncryptionProperties = new AES256EncryptionProperties(VALID_AES_256_TEST_SECRET_KEY);
    private static final AES256Encryption aes256Encryption;
    private static final Base64.Encoder ENCODER = Base64.getEncoder();

    static {
        try {
            aes256Encryption = new AES256Encryption(aes256EncryptionProperties);
        } catch (GeneralSecurityException e) {
            throw new TestAbortedException("Failed to prepare AES256EncryptionTest", e);
        }
    }

    @Test
    void encrypt_decrypt_returns_same_value() {
        /* prepare */
        String plainText = "test";

        /* execute */
        byte[] encryptedTextBytes = aes256Encryption.encrypt(plainText);
        String decryptedText = aes256Encryption.decrypt(encryptedTextBytes);

        /* test */
        assertThat(decryptedText).isEqualTo(plainText);
    }

    @Test
    void encrypt_returns_correctly_encrypted_text() {
        /* prepare */
        String plainText = "test";

        /* execute */
        byte[] encryptedTextBytes = aes256Encryption.encrypt(plainText);

        /* test */
        String encryptedTextB64Encoded = ENCODER.encodeToString(encryptedTextBytes);
        String expected = "SBRD1/R10NQuOFBdpC0S0g==";
        assertThat(encryptedTextB64Encoded).isEqualTo(expected);
    }

    @Test
    void encrypt_exceptions_are_handled_well() {
        /* execute & test */

        /* @formatter:off */
        assertThatThrownBy(() -> aes256Encryption.encrypt(null))
                .isInstanceOf(AES256EncryptionException.class)
                .hasMessageContaining("Failed to encrypt text");
        /* @formatter:on */
    }

    @Test
    void decrypt_exceptions_are_handled_well() {
        /* execute & test */

        /* @formatter:off */
        assertThatThrownBy(() -> aes256Encryption.decrypt(null))
                .isInstanceOf(AES256EncryptionException.class)
                .hasMessageContaining("Failed to decrypt text");
        /* @formatter:on */
    }
}
