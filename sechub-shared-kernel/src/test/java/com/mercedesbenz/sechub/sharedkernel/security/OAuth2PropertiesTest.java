// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.mercedesbenz.sechub.testframework.spring.YamlPropertyLoaderFactory;

@SpringBootTest
@ActiveProfiles("oauth2")
@TestPropertySource(locations = "classpath:application-test.yml", factory = YamlPropertyLoaderFactory.class)
class OAuth2PropertiesTest {

    private final OAuth2Properties properties;

    @Autowired
    OAuth2PropertiesTest(OAuth2Properties properties) {
        this.properties = properties;
    }

    @Test
    void construct_o_auth_2_properties_with_valid_properties_file_succeeds() {
        assertThat(properties.getJwkSetUri()).isEqualTo("https://example.org/jwk-set-uri");
    }

    /* @formatter:off */
    @Test
    void construct_o_auth_2_properties_with_null_jwk_set_uri_property_fails() {
        assertThatThrownBy(() -> new OAuth2Properties(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("The property 'sechub.security.oauth2.jwk-set-uri' must not be null");
    }
    /* @formatter:on */

    @Configuration
    @EnableConfigurationProperties(OAuth2Properties.class)
    static class TestConfig {
    }
}