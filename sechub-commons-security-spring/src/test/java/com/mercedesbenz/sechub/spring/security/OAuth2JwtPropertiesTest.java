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
    private final OAuth2OpaqueTokenProperties oAuth2OpaqueTokenProperties;

    OAuth2JwtPropertiesTest(@Autowired OAuth2JwtProperties oAuth2JwtProperties,
            @Autowired(required = false) OAuth2OpaqueTokenProperties oAuth2OpaqueTokenProperties) {
        this.oAuth2JwtProperties = oAuth2JwtProperties;
        this.oAuth2OpaqueTokenProperties = oAuth2OpaqueTokenProperties;
    }

    @Test
    void construct_properties_with_jwt_enabled_succeeds() {
        assertThat(oAuth2JwtProperties.isEnabled()).isTrue();
        assertThat(oAuth2JwtProperties.getJwkSetUri()).isEqualTo("https://example.org/jwk-set-uri");
    }

    @Test
    void construct_properties_with_jwt_enabled_opaque_token_properties_is_null() {
        assertThat(oAuth2OpaqueTokenProperties).isNull();
    }

    /* @formatter:off */
    @Test
    void construct_properties_with_enabled_null_uri_fails() {
        assertThatThrownBy(() -> new OAuth2JwtProperties(null, "https://example.org/jwk-set-uri"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Property 'sechub.security.oauth2.jwt.enabled' must not be null");
    }
    /* @formatter:on */

    /* @formatter:off */
    @Test
    void construct_properties_with_jwk_set_uri_null_fails() {
        assertThatThrownBy(() -> new OAuth2JwtProperties(Boolean.TRUE, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Property 'sechub.security.oauth2.jwt.jwk-set-uri' must not be null");
    }
    /* @formatter:on */

    @Configuration
    @Import({ OAuth2JwtPropertiesConfiguration.class, OAuth2OpaqueTokenPropertiesConfiguration.class })
    static class TestConfig {
    }
}