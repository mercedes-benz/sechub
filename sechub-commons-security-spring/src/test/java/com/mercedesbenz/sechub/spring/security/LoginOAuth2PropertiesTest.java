// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
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
import org.springframework.test.context.TestPropertySource;

import com.mercedesbenz.sechub.testframework.spring.YamlPropertyLoaderFactory;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-login-oauth2-test.yaml", factory = YamlPropertyLoaderFactory.class)
class LoginOAuth2PropertiesTest {

    private static final String ERR_MSG_FORMAT = "The property 'sechub.security.login.oauth2.%s' must not be null";

    private final LoginOAuth2Properties properties;

    @Autowired
    LoginOAuth2PropertiesTest(LoginOAuth2Properties properties) {
        this.properties = properties;
    }

    @Test
    void construct_login_o_auth_2_properties_with_valid_properties_file_succeeds() {
        assertThat(properties.getClientId()).isEqualTo("client-id");
        assertThat(properties.getClientSecret()).isEqualTo("client-secret");
        assertThat(properties.getProvider()).isEqualTo("provider");
        assertThat(properties.getRedirectUri()).isEqualTo("redirect-uri");
        assertThat(properties.getIssuerUri()).isEqualTo("issuer-uri");
        assertThat(properties.getAuthorizationUri()).isEqualTo("authorization-uri");
        assertThat(properties.getTokenUri()).isEqualTo("token-uri");
        assertThat(properties.getUserInfoUri()).isEqualTo("user-info-uri");
    }

    /* @formatter:off */
    @ParameterizedTest
    @ArgumentsSource(InvalidLoginOAuth2PropertiesProvider.class)
    void construct_login_o_auth_2_properties_with_null_property_fails(String clientId,
                                                                      String clientSecret,
                                                                      String provider,
                                                                      String redirectUri,
                                                                      String issuerUri,
                                                                      String authorizationUri,
                                                                      String tokenUri,
                                                                      String userInfoUri,
                                                                      String errMsg) {
        Assertions.assertThatThrownBy(() -> new LoginOAuth2Properties(clientId, clientSecret, provider, redirectUri, issuerUri, authorizationUri, tokenUri, userInfoUri))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining(errMsg);
    }
    /* @formatter:on */

    @Configuration
    @Import(LoginOAuth2PropertiesConfiguration.class)
    static class TestConfig {
    }

    private static class InvalidLoginOAuth2PropertiesProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            /* @formatter:off */
            return Stream.of(
                    Arguments.of(null, "client-secret", "provider", "redirect-uri", "issuer-uri", "authorization-uri", "token-uri", "user-info-uri", String.format(ERR_MSG_FORMAT, "client-id")),
                    Arguments.of("client-id", null, "provider", "redirect-uri", "issuer-uri", "authorization-uri", "token-uri", "user-info-uri", String.format(ERR_MSG_FORMAT, "client-secret")),
                    Arguments.of("client-id", "client-secret", null, "redirect-uri", "issuer-uri", "authorization-uri", "token-uri", "user-info-uri", String.format(ERR_MSG_FORMAT, "provider")),
                    Arguments.of("client-id", "client-secret", "provider", null, "issuer-uri", "authorization-uri", "token-uri", "user-info-uri", String.format(ERR_MSG_FORMAT, "redirect-uri")),
                    Arguments.of("client-id", "client-secret", "provider", "redirect-uri", null, "authorization-uri", "token-uri", "user-info-uri", String.format(ERR_MSG_FORMAT, "issuer-uri")),
                    Arguments.of("client-id", "client-secret", "provider", "redirect-uri", "issuer-uri", null, "token-uri", "user-info-uri", String.format(ERR_MSG_FORMAT, "authorization-uri")),
                    Arguments.of("client-id", "client-secret", "provider", "redirect-uri", "issuer-uri", "authorization-uri", null, "user-info-uri", String.format(ERR_MSG_FORMAT, "token-uri")),
                    Arguments.of("client-id", "client-secret", "provider", "redirect-uri", "issuer-uri", "authorization-uri", "token-uri", null, String.format(ERR_MSG_FORMAT, "user-info-uri"))
            );
            /* @formatter:on */
        }
    }
}
