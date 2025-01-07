// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.mercedesbenz.sechub.testframework.spring.YamlPropertyLoaderFactory;

@SpringBootTest
@ActiveProfiles("oauth2")
@TestPropertySource(locations = "classpath:application-jwt-test.yml", factory = YamlPropertyLoaderFactory.class)
class OAuth2JwtPropertiesTest {

    private final OAuth2JwtProperties oAuth2JwtProperties;

    OAuth2JwtPropertiesTest(@Autowired OAuth2JwtProperties oAuth2JwtProperties) {
        this.oAuth2JwtProperties = oAuth2JwtProperties;
    }

    @Test
    void construct_properties_with_jwt_enabled_succeeds() {
        assertThat(oAuth2JwtProperties.getJwkSetUri()).isEqualTo("https://example.org/jwk-set-uri");
    }

    /* @formatter:off */
    @Test
    void construct_properties_with_jwk_set_uri_null_fails() {
        assertThatThrownBy(() -> new OAuth2JwtProperties(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Property 'sechub.security.oauth2.jwt.jwk-set-uri' must not be null");
    }
    /* @formatter:on */

    @Configuration
    @Import({ OAuth2JwtPropertiesConfiguration.class, OAuth2OpaqueTokenPropertiesConfiguration.class })
    static class TestConfig {
    }
}