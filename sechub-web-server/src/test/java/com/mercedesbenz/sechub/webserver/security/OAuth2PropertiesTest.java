// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.mercedesbenz.sechub.testframework.spring.YamlPropertyLoaderFactory;

@SpringBootTest
@ActiveProfiles("oauth2-enabled")
@TestPropertySource(locations = "classpath:application-test.yml", factory = YamlPropertyLoaderFactory.class)
class OAuth2PropertiesTest {

    private static final String ERR_MSG_FORMAT = "The property 'sechub.security.oauth2.%s' must not be null";

    private final OAuth2Properties properties;

    @Autowired
    OAuth2PropertiesTest(OAuth2Properties properties) {
        this.properties = properties;
    }

    @Test
    void construct_o_auth_2_properties_with_valid_properties_file_succeeds() {
        assertThat(properties.getClientId()).isEqualTo("client-id");
        assertThat(properties.getClientSecret()).isEqualTo("client-secret");
        assertThat(properties.getProvider()).isEqualTo("provider");
        assertThat(properties.getRedirectUri()).isEqualTo("redirect-uri");
        assertThat(properties.getIssuerUri()).isEqualTo("issuer-uri");
        assertThat(properties.getAuthorizationUri()).isEqualTo("authorization-uri");
        assertThat(properties.getTokenUri()).isEqualTo("token-uri");
        assertThat(properties.getUserInfoUri()).isEqualTo("user-info-uri");
        assertThat(properties.getJwkSetUri()).isEqualTo("https://example.org/jwk-set-uri");
    }

    /* @formatter:off */
    @ParameterizedTest
    @ArgumentsSource(InvalidOAuth2PropertiesProvider.class)
    void construct_o_auth_2_properties_with_null_property_fails(String clientId,
                                                                String clientSecret,
                                                                String provider,
                                                                String redirectUri,
                                                                String issuerUri,
                                                                String authorizationUri,
                                                                String tokenUri,
                                                                String userInfoUri,
                                                                String jwkSetUri,
                                                                String errMsg) {
        assertThatThrownBy(() -> new OAuth2Properties(clientId, clientSecret, provider, redirectUri, issuerUri, authorizationUri, tokenUri, userInfoUri, jwkSetUri))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining(errMsg);
    }
    /* @formatter:on */

    @Configuration
    @Import(OAuth2PropertiesConfig.class)
    static class TestConfig {
    }

    private static class InvalidOAuth2PropertiesProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            /* @formatter:off */
            return Stream.of(
                    Arguments.of(null, "client-secret", "provider", "redirect-uri", "issuer-uri", "authorization-uri", "token-uri", "user-info-uri", "jwk-set-uri", ERR_MSG_FORMAT.formatted("client-id")),
                    Arguments.of("client-id", null, "provider", "redirect-uri", "issuer-uri", "authorization-uri", "token-uri", "user-info-uri", "jwk-set-uri", ERR_MSG_FORMAT.formatted("client-secret")),
                    Arguments.of("client-id", "client-secret", null, "redirect-uri", "issuer-uri", "authorization-uri", "token-uri", "user-info-uri", "jwk-set-uri", ERR_MSG_FORMAT.formatted("provider")),
                    Arguments.of("client-id", "client-secret", "provider", null, "issuer-uri", "authorization-uri", "token-uri", "user-info-uri", "jwk-set-uri", ERR_MSG_FORMAT.formatted("redirect-uri")),
                    Arguments.of("client-id", "client-secret", "provider", "redirect-uri", null, "authorization-uri", "token-uri", "user-info-uri", "jwk-set-uri", ERR_MSG_FORMAT.formatted("issuer-uri")),
                    Arguments.of("client-id", "client-secret", "provider", "redirect-uri", "issuer-uri", null, "token-uri", "user-info-uri", "jwk-set-uri", ERR_MSG_FORMAT.formatted("authorization-uri")),
                    Arguments.of("client-id", "client-secret", "provider", "redirect-uri", "issuer-uri", "authorization-uri", null, "user-info-uri", "jwk-set-uri", ERR_MSG_FORMAT.formatted("token-uri")),
                    Arguments.of("client-id", "client-secret", "provider", "redirect-uri", "issuer-uri", "authorization-uri", "token-uri", null, "jwk-set-uri", ERR_MSG_FORMAT.formatted("user-info-uri")),
                    Arguments.of("client-id", "client-secret", "provider", "redirect-uri", "issuer-uri", "authorization-uri", "token-uri", "user-info-uri", null, ERR_MSG_FORMAT.formatted("jwk-set-uri")));
            /* @formatter:on */
        }
    }
}
