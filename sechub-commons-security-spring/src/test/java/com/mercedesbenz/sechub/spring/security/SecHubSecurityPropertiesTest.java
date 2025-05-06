// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

import com.mercedesbenz.sechub.testframework.spring.YamlPropertyLoaderFactory;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-security-properties-test.yaml", factory = YamlPropertyLoaderFactory.class)
class SecHubSecurityPropertiesTest {

    private final SecHubSecurityProperties properties;

    @Autowired
    SecHubSecurityPropertiesTest(SecHubSecurityProperties properties) {
        this.properties = properties;
    }

    @Test
    void construct_security_properties_with_valid_properties_file_succeeds() {
        SecHubSecurityProperties.ResourceServerProperties server = properties.getResourceServerProperties();
        assertThat(server).isNotNull();
        assertThat(server.isOAuth2ModeEnabled()).isTrue();
        assertThat(server.isClassicModeEnabled()).isTrue();
        SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties oAuth2 = server.getOAuth2Properties();
        assertThat(oAuth2).isNotNull();
        assertThat(oAuth2.getMode()).isEqualTo("jwt");
        assertThat(oAuth2.isJwtModeEnabled()).isTrue();
        assertThat(oAuth2.isOpaqueTokenModeEnabled()).isFalse();
        SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties.JwtProperties jwt = oAuth2.getJwtProperties();
        assertThat(jwt).isNotNull();
        assertThat(jwt.getJwkSetUri()).isEqualTo("https://example.org/jwk-set-uri");
        SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties.OpaqueTokenProperties opaqueToken = oAuth2.getOpaqueTokenProperties();
        assertThat(opaqueToken).isNull();

        SecHubSecurityProperties.LoginProperties login = properties.getLoginProperties();
        assertThat(login).isNotNull();
        assertThat(login.isEnabled()).isTrue();
        assertThat(login.getLoginPage()).isEqualTo("/login");
        assertThat(login.getRedirectUri()).isEqualTo("example.org/redirect-uri");
        assertThat(login.getModes()).containsExactly("oauth2", "classic");
        SecHubSecurityProperties.LoginProperties.OAuth2Properties loginOAuth2 = login.getOAuth2Properties();
        assertThat(loginOAuth2).isNotNull();
        assertThat(loginOAuth2.getClientId()).isEqualTo("example-client-id");
        assertThat(loginOAuth2.getClientSecret()).isEqualTo("example-client-secret");
        assertThat(loginOAuth2.getProvider()).isEqualTo("example-provider");
        assertThat(loginOAuth2.getRedirectUri()).isEqualTo("https://example.org/redirect-uri");
        assertThat(loginOAuth2.getIssuerUri()).isEqualTo("https://example.org/issuer-uri");
        assertThat(loginOAuth2.getAuthorizationUri()).isEqualTo("https://example.org/authorization-uri");
        assertThat(loginOAuth2.getTokenUri()).isEqualTo("https://example.org/token-uri");
        assertThat(loginOAuth2.getUserInfoUri()).isEqualTo("https://example.org/user-info-uri");
        assertThat(loginOAuth2.getJwkSetUri()).isEqualTo("https://example.org/jwk-set-uri");

        SecHubSecurityProperties.EncryptionProperties encryption = properties.getEncryptionProperties();
        assertThat(encryption).isNotNull();
        assertThat(encryption.getSecretKey()).isEqualTo("test-test-test-test-test-test-32");
    }

    @Test
    void construct_security_properties_with_null_server_is_ok() {
        /* execute + test */
        assertDoesNotThrow(() -> new SecHubSecurityProperties(null, null, null, null));
    }

    @Test
    void construct_security_properties_with_null_login_is_ok() {
        /* execute + test */
        assertDoesNotThrow(() -> new SecHubSecurityProperties(mock(), null, null, null));
    }

    @Test
    void construct_security_properties_with_login_enabled_and_null_encryption_fails() {
        /* prepare */
        SecHubSecurityProperties.LoginProperties loginMock = mock();
        when(loginMock.isEnabled()).thenReturn(true);

        /* execute + test */
        /* @formatter:off */
        assertThatThrownBy(() -> new SecHubSecurityProperties(mock(), loginMock, null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("The property 'sechub.security.encryption' must not be null");
        /* @formatter:on */
    }

    @Test
    void construct_server_properties_with_valid_arguments_succeeds() {
        /* prepare */
        String modes = "oauth2, classic";
        SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties oAuth2 = mock();

        /* execute */
        SecHubSecurityProperties.ResourceServerProperties server = new SecHubSecurityProperties.ResourceServerProperties(modes, oAuth2);

        /* test */
        assertThat(server.getModes()).isEqualTo(Set.of("oauth2", "classic"));
        assertThat(server.isOAuth2ModeEnabled()).isTrue();
        assertThat(server.isClassicModeEnabled()).isTrue();
        assertThat(server.getOAuth2Properties()).isEqualTo(oAuth2);
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    void construct_server_properties_with_non_existing_modes_fails(String modes) {

        /* execute + test */
        /* @formatter:off */
        assertThatThrownBy(() -> new SecHubSecurityProperties.ResourceServerProperties(modes, mock()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The property 'sechub.security.server.modes' must at least include 'oauth2' or 'classic' mode");
        /* @formatter:on */
    }

    @Test
    void construct_server_properties_with_invalid_modes_fails() {
        /* prepare */
        String modes = "invalid";

        /* execute + test */
        /* @formatter:off */
        assertThatThrownBy(() -> new SecHubSecurityProperties.ResourceServerProperties(modes, mock()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The property 'sechub.security.server.modes' allows only 'oauth2' or 'classic' mode");
        /* @formatter:on */
    }

    @Test
    void construct_server_oauth2_properties_with_jwt_mode_succeeds() {
        /* prepare */
        String mode = "jwt";
        SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties.JwtProperties jwt = mock();

        /* execute */
        SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties oAuth2 = new SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties(mode,
                jwt, null);

        /* test */
        assertThat(oAuth2.getMode()).isEqualTo(mode);
        assertThat(oAuth2.isJwtModeEnabled()).isTrue();
        assertThat(oAuth2.isOpaqueTokenModeEnabled()).isFalse();
        assertThat(oAuth2.getJwtProperties()).isEqualTo(jwt);
        assertThat(oAuth2.getOpaqueTokenProperties()).isNull();
    }

    @Test
    void construct_server_oauth2_properties_with_opaque_token_mode_succeeds() {
        /* prepare */
        String mode = "opaque-token";
        SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties.OpaqueTokenProperties opaqueToken = mock();

        /* execute */
        SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties oAuth2 = new SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties(mode,
                null, opaqueToken);

        /* test */
        assertThat(oAuth2.getMode()).isEqualTo(mode);
        assertThat(oAuth2.isJwtModeEnabled()).isFalse();
        assertThat(oAuth2.isOpaqueTokenModeEnabled()).isTrue();
        assertThat(oAuth2.getJwtProperties()).isNull();
        assertThat(oAuth2.getOpaqueTokenProperties()).isEqualTo(opaqueToken);
    }

    @Test
    void construct_server_oauth2_properties_with_null_mode_fails() {
        /* execute + test */
        /* @formatter:off */
        assertThatThrownBy(() -> new SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties(null, mock(), mock()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("The property 'sechub.security.server.oauth2.mode' must not be null");
        /* @formatter:on */
    }

    @Test
    void construct_server_oauth2_properties_with_invalid_mode_fails() {
        /* execute + test */
        /* @formatter:off */
        assertThatThrownBy(() -> new SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties("invalid", mock(), mock()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The property 'sechub.security.server.oauth2.mode' allows only 'jwt' or 'opaque-token' mode");
        /* @formatter:on */
    }

    @Test
    void construct_jwt_properties_with_valid_arguments_succeeds() {
        /* prepare */
        String jwkSetUri = "https://example.org/jwk-set-uri";

        /* execute */
        SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties.JwtProperties jwt = new SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties.JwtProperties(
                jwkSetUri);

        /* test */
        assertThat(jwt.getJwkSetUri()).isEqualTo(jwkSetUri);
    }

    @Test
    void construct_jwt_properties_with_null_jwk_set_uri_fails() {
        /* execute + test */
        /* @formatter:off */
        assertThatThrownBy(() -> new SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties.JwtProperties(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("The property 'sechub.security.server.oauth2.jwt.jwk-set-uri' must not be null");
        /* @formatter:on */
    }

    @Test
    void construct_opaque_token_properties_with_valid_arguments_succeeds() {
        /* prepare */
        String introspectionUri = "https://example.org/introspection-uri";
        String clientId = "example-client-id";
        String clientSecret = "example-client-secret";
        Duration defaultTokenExpiresIn = Duration.ofDays(1);
        Duration maxCacheDuration = Duration.ofDays(30);

        /* execute */
        SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties.OpaqueTokenProperties opaqueToken = new SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties.OpaqueTokenProperties(
                introspectionUri, clientId, clientSecret, defaultTokenExpiresIn, maxCacheDuration);

        /* test */
        assertThat(opaqueToken.getIntrospectionUri()).isEqualTo(introspectionUri);
        assertThat(opaqueToken.getClientId()).isEqualTo(clientId);
        assertThat(opaqueToken.getClientSecret()).isEqualTo(clientSecret);
        assertThat(opaqueToken.getMaxCacheDuration()).isEqualTo(maxCacheDuration);
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidOpaqueTokenPropertiesProvider.class)
    /* @formatter:off */
    void construct_opaque_token_properties_with_null_arguments_fails(String introspectionUri,
                                                                     String clientId,
                                                                     String clientSecret,
                                                                     Duration maxCacheDuration,
                                                                     String errMsg) {
        /* prepare */

        /* 'defaultTokenExpiresIn' is nullable */
        Duration defaultTokenExpiresIn = null;

        /* execute + test */
        assertThatThrownBy(() -> new SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties.OpaqueTokenProperties(introspectionUri, clientId, clientSecret, defaultTokenExpiresIn, maxCacheDuration))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining(errMsg);
        /* @formatter:on */
    }

    @Test
    void construct_login_properties_with_valid_arguments_succeeds() {
        /* prepare */
        boolean enabled = true;
        String loginPage = "/login";
        String redirectUri = "example.org/redirect-uri";
        String modes = "oauth2, classic";
        SecHubSecurityProperties.LoginProperties.OAuth2Properties oAuth2 = mock();
        SecHubSecurityProperties.LoginProperties.ClassicAuthProperties classicAuth = mock();

        /* execute */
        SecHubSecurityProperties.LoginProperties login = new SecHubSecurityProperties.LoginProperties(enabled, loginPage, redirectUri, modes, oAuth2,
                classicAuth);

        /* test */
        assertThat(login.isEnabled()).isEqualTo(enabled);
        assertThat(login.getLoginPage()).isEqualTo(loginPage);
        assertThat(login.getRedirectUri()).isEqualTo(redirectUri);
        assertThat(login.getModes()).isEqualTo(Set.of("oauth2", "classic"));
        assertThat(login.getOAuth2Properties()).isEqualTo(oAuth2);
        assertThat(login.getClassicAuthProperties()).isEqualTo(classicAuth);
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidLoginPropertiesProvider.class)
    /* @formatter:off */
    void construct_login_properties_with_null_property_fails(Boolean enabled,
                                                             String loginPage,
                                                             String redirectUri,
                                                             String modes,
                                                             SecHubSecurityProperties.LoginProperties.OAuth2Properties oAuth2,
                                                             Class<? extends Exception> exceptionClazz,
                                                             String errMsg) {
        /* prepare */
        /* classic auth properties are nullable therefore not tested here */
        SecHubSecurityProperties.LoginProperties.ClassicAuthProperties classicAuth = null;

        /* execute + test */
        assertThatThrownBy(() -> new SecHubSecurityProperties.LoginProperties(enabled, loginPage, redirectUri, modes, oAuth2, classicAuth))
                .isInstanceOf(exceptionClazz)
                .hasMessageContaining(errMsg);
        /* @formatter:on */
    }

    @Test
    void construct_login_properties_with_null_classic_auth_properties_constructs_default_classic_auth_properties() {
        /* prepare */
        boolean enabled = true;
        String loginPage = "/login";
        String redirectUri = "example.org/redirect-uri";
        String modes = "oauth2, classic";
        SecHubSecurityProperties.LoginProperties.OAuth2Properties oAuth2 = mock();

        /* execute */
        SecHubSecurityProperties.LoginProperties login = new SecHubSecurityProperties.LoginProperties(enabled, loginPage, redirectUri, modes, oAuth2, null);

        /* test */
        Duration expectedCookieAge = Duration.ofHours(24);
        SecHubSecurityProperties.LoginProperties.ClassicAuthProperties classicAuthProperties = login.getClassicAuthProperties();
        assertThat(classicAuthProperties).isNotNull();
        assertThat(classicAuthProperties.getCookieAge()).isEqualTo(expectedCookieAge);
        assertThat(classicAuthProperties.getCookieAgeSeconds()).isEqualTo(expectedCookieAge.getSeconds());
    }

    @Test
    void construct_login_oauth2_properties_with_valid_arguments_succeeds() {
        /* prepare */
        String clientId = "example-client-id";
        String clientSecret = "example-client-secret";
        String provider = "example-provider";
        String redirectUri = "https://example.org/redirect-uri";
        String issuerUri = "https://example.org/issuer-uri";
        String authorizationUri = "https://example.org/authorization-uri";
        String tokenUri = "https://example.org/token-uri";
        String userInfoUri = "https://example.org/user-info-uri";
        String jwkSetUri = "https://example.org/jwk-set-uri";

        /* execute */
        SecHubSecurityProperties.LoginProperties.OAuth2Properties oAuth2 = new SecHubSecurityProperties.LoginProperties.OAuth2Properties(clientId, clientSecret,
                provider, redirectUri, issuerUri, authorizationUri, tokenUri, userInfoUri, jwkSetUri);

        /* test */
        assertThat(oAuth2.getClientId()).isEqualTo(clientId);
        assertThat(oAuth2.getClientSecret()).isEqualTo(clientSecret);
        assertThat(oAuth2.getProvider()).isEqualTo(provider);
        assertThat(oAuth2.getRedirectUri()).isEqualTo(redirectUri);
        assertThat(oAuth2.getIssuerUri()).isEqualTo(issuerUri);
        assertThat(oAuth2.getAuthorizationUri()).isEqualTo(authorizationUri);
        assertThat(oAuth2.getTokenUri()).isEqualTo(tokenUri);
        assertThat(oAuth2.getUserInfoUri()).isEqualTo(userInfoUri);
        assertThat(oAuth2.getJwkSetUri()).isEqualTo(jwkSetUri);
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidLoginOAuth2PropertiesProvider.class)
    /* @formatter:off */
    void construct_login_oauth2_properties_with_null_arguments_fails(String clientId,
                                                                     String clientSecret,
                                                                     String provider,
                                                                     String redirectUri,
                                                                     String issuerUri,
                                                                     String authorizationUri,
                                                                     String tokenUri,
                                                                     String userInfoUri,
                                                                     String jwkSetUri,
                                                                     String errMsg) {
        /* execute + test */
        assertThatThrownBy(() -> new SecHubSecurityProperties.LoginProperties.OAuth2Properties(clientId, clientSecret, provider, redirectUri, issuerUri, authorizationUri, tokenUri, userInfoUri, jwkSetUri))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining(errMsg);
        /* @formatter:on */
    }

    @Test
    void construct_encryption_properties_with_valid_arguments_succeeds() {
        /* prepare */
        String secretKey = "test-test-test-test-test-test-32";

        /* execute */
        SecHubSecurityProperties.EncryptionProperties encryption = new SecHubSecurityProperties.EncryptionProperties(secretKey);

        /* test */
        assertThat(encryption.getSecretKey()).isEqualTo(secretKey);
    }

    @Test
    void construct_encryption_properties_with_null_secret_key_fails() {
        /* execute + test */
        /* @formatter:off */
        assertThatThrownBy(() -> new SecHubSecurityProperties.EncryptionProperties(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("The property 'sechub.security.encryption.secret-key' must not be null");
        /* @formatter:on */
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "1", "est-test-test-test-test-test-31", "-test-test-test-test-test-test-33" })
    void construct_aes256encryption_properties_with_non_256_bit_long_secret_key_fails(String secretKey) {
        /* @formatter:off */
        /* execute & test */
        assertThatThrownBy(() -> new SecHubSecurityProperties.EncryptionProperties(secretKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The property sechub.security.encryption.secret-key must be a 256-bit string");
        /* @formatter:on */
    }

    @Configuration
    @EnableConfigurationProperties(SecHubSecurityProperties.class)
    static class TestConfiguration {

    }

    private static class InvalidOpaqueTokenPropertiesProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            return Stream.of(
                    Arguments.of(null, "example-client-id", "example-client-secret", Duration.ofDays(30), "The property 'sechub.security.server.oauth2.opaque-token.introspection-uri' must not be null"),
                    Arguments.of("https://example.org/introspection-uri", null, "example-client-secret", Duration.ofDays(30), "The property 'sechub.security.server.oauth2.opaque-token.client-id' must not be null"),
                    Arguments.of("https://example.org/introspection-uri", "example-client-id", null, Duration.ofDays(30), "The property 'sechub.security.server.oauth2.opaque-token.client-secret' must not be null"),
                    Arguments.of("https://example.org/introspection-uri", "example-client-id", "example-client-secret", null, "The property 'sechub.security.server.oauth2.opaque-token.max-cache-duration' must not be null")
            );
        }
        /* @formatter:on */
    }

    private static class InvalidLoginPropertiesProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            return Stream.of(
                    Arguments.of(null, "/login", "example.org/redirect-uri", "oauth2,classic", mock(SecHubSecurityProperties.LoginProperties.OAuth2Properties.class), NullPointerException.class,"The property 'sechub.security.login.enabled' must not be null"),
                    Arguments.of(true, null, "example.org/redirect-uri","oauth2,classic", mock(SecHubSecurityProperties.LoginProperties.OAuth2Properties.class),  NullPointerException.class, "The property 'sechub.security.login.login-page' must not be null"),
                    Arguments.of(true, "/login", null, "oauth2, classic", mock(SecHubSecurityProperties.LoginProperties.OAuth2Properties.class), NullPointerException.class, "The property 'sechub.security.login.redirect-uri' must not be null"),
                    Arguments.of(true, "/login", "example.org/redirect-uri", null, mock(SecHubSecurityProperties.LoginProperties.OAuth2Properties.class),  IllegalArgumentException.class, "The property 'sechub.security.login.modes' must not be empty or null"),
                    Arguments.of(true, "/login", "example.org/redirect-uri", "oauth2,classic", null,  NullPointerException.class,"The property 'sechub.security.login.oauth2' must not be null")
            );
        }
        /* @formatter:on */
    }

    private static class InvalidLoginOAuth2PropertiesProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            return Stream.of(
                    Arguments.of(null, "example-client-secret", "example-provider", "https://example.org/redirect-uri", "https://example.org/issuer-uri", "https://example.org/authorization-uri", "https://example.org/token-uri", "https://example.org/user-info-uri", "https://example.org/jwk-set-uri", "The property 'sechub.security.login.oauth2.client-id' must not be null"),
                    Arguments.of("example-client-id", null, "example-provider", "https://example.org/redirect-uri", "https://example.org/issuer-uri", "https://example.org/authorization-uri", "https://example.org/token-uri", "https://example.org/user-info-uri", "https://example.org/jwk-set-uri", "The property 'sechub.security.login.oauth2.client-secret' must not be null"),
                    Arguments.of("example-client-id", "example-client-secret", null, "https://example.org/redirect-uri", "https://example.org/issuer-uri", "https://example.org/authorization-uri", "https://example.org/token-uri", "https://example.org/user-info-uri", "https://example.org/jwk-set-uri", "The property 'sechub.security.login.oauth2.provider' must not be null"),
                    Arguments.of("example-client-id", "example-client-secret", "example-provider", null, "https://example.org/issuer-uri", "https://example.org/authorization-uri", "https://example.org/token-uri", "https://example.org/user-info-uri", "https://example.org/jwk-set-uri", "The property 'sechub.security.login.oauth2.redirect-uri' must not be null"),
                    Arguments.of("example-client-id", "example-client-secret", "example-provider", "https://example.org/redirect-uri", null, "https://example.org/authorization-uri", "https://example.org/token-uri", "https://example.org/user-info-uri", "https://example.org/jwk-set-uri", "The property 'sechub.security.login.oauth2.issuer-uri' must not be null"),
                    Arguments.of("example-client-id", "example-client-secret", "example-provider", "https://example.org/redirect-uri", "https://example.org/issuer-uri", null, "https://example.org/token-uri", "https://example.org/user-info-uri", "https://example.org/jwk-set-uri", "The property 'sechub.security.login.oauth2.authorization-uri' must not be null"),
                    Arguments.of("example-client-id", "example-client-secret", "example-provider", "https://example.org/redirect-uri", "https://example.org/issuer-uri", "https://example.org/authorization-uri", null, "https://example.org/user-info-uri", "https://example.org/jwk-set-uri", "The property 'sechub.security.login.oauth2.token-uri' must not be null"),
                    Arguments.of("example-client-id", "example-client-secret", "example-provider", "https://example.org/redirect-uri", "https://example.org/issuer-uri", "https://example.org/authorization-uri", "https://example.org/token-uri", null, "https://example.org/jwk-set-uri", "The property 'sechub.security.login.oauth2.user-info-uri' must not be null"),
                    Arguments.of("example-client-id", "example-client-secret", "example-provider", "https://example.org/redirect-uri", "https://example.org/issuer-uri", "https://example.org/authorization-uri", "https://example.org/token-uri", "https://example.org/user-info-uri", null, "The property 'sechub.security.login.oauth2.jwk-set-uri' must not be null")
            );
        }
    }
}