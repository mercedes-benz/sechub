package com.mercedesbenz.sechub.webserver.encryption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.GeneralSecurityException;

import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

class AES256EncryptionTest {

    private static final String VALID_AES_256_TEST_SECRET_KEY = "test-test-test-test-test-test-32";
    private static final AES256EncryptionProperties aes256EncryptionProperties = new AES256EncryptionProperties(VALID_AES_256_TEST_SECRET_KEY);
    private static final AES256Encryption aes256Encryption;
    static {
        try {
            aes256Encryption = new AES256Encryption(aes256EncryptionProperties);
        } catch (GeneralSecurityException e) {
            throw new TestAbortedException("Failed to prepare AES256EncryptionTest", e);
        }
    }

    @Test
    void encrypt_decrypt_returns_same_value() {
        // prepare
        String plainText = "test";

        // execute
        String encryptedTextB64Encoded = aes256Encryption.encrypt(plainText);
        String decryptedText = aes256Encryption.decrypt(encryptedTextB64Encoded);

        // test
        assertThat(plainText).isEqualTo(decryptedText);
    }

    @Test
    void encrypt_returns_correctly_encrypted_text() {
        // prepare
        String plainText = "test";

        // execute
        String encryptedTextB64Encoded = aes256Encryption.encrypt(plainText);

        // test
        assertThat(encryptedTextB64Encoded).isEqualTo("SBRD1/R10NQuOFBdpC0S0g==");
    }

    @Test
    void encrypt_exceptions_are_handled_well() {
        // execute & assert
        assertThatThrownBy(() -> aes256Encryption.encrypt(null)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to encrypt text: Cannot invoke \"String.getBytes(java.nio.charset.Charset)\" because \"plainText\" is null");
    }

    @Test
    void decrypt_exceptions_are_handled_well() {
        // execute & assert
        assertThatThrownBy(() -> aes256Encryption.decrypt(null)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to decrypt text: Cannot invoke \"String.getBytes(java.nio.charset.Charset)\" because \"src\" is null");
    }
}
