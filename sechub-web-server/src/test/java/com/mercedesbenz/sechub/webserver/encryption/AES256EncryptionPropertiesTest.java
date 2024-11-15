// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.encryption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

import com.mercedesbenz.sechub.testframework.spring.YamlPropertyLoaderFactory;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml", factory = YamlPropertyLoaderFactory.class)
class AES256EncryptionPropertiesTest {

    private static final String VALID_AES_256_TEST_SECRET_KEY = "test-test-test-test-test-test-32";

    private final AES256EncryptionProperties properties;

    @Autowired
    AES256EncryptionPropertiesTest(AES256EncryptionProperties properties) {
        this.properties = properties;
    }

    @Test
    void construct_aes256_encryption_properties_with_valid_properties_file_succeeds() {
        assertThat(properties.getSecretKeyBytes()).isEqualTo(VALID_AES_256_TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void construct_aes256encryption_properties_with_null_secret_key_fails() {
        /* @formatter:off */
        /* execute & test */
        assertThatThrownBy(() -> new AES256EncryptionProperties(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("The property 'sechub.security.encryption.secret-key' must not be null");
        /* @formatter:on */
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "1", "est-test-test-test-test-test-31", "-test-test-test-test-test-test-33" })
    void construct_aes256encryption_properties_with_non_256_bit_long_secret_key_fails(String secretKey) {
        /* @formatter:off */
        /* execute & test */
        assertThatThrownBy(() -> new AES256EncryptionProperties(secretKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The property sechub.security.encryption.secret-key must be a 256-bit string");
        /* @formatter:on */
    }

    @Configuration
    @EnableConfigurationProperties(AES256EncryptionProperties.class)
    static class TestConfig {
    }
}
