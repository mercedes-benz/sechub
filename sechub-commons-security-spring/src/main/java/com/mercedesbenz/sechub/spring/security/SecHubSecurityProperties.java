// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;

import java.time.Duration;
import java.util.Set;

import javax.crypto.SealedObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

@ConfigurationProperties(prefix = SecHubSecurityProperties.PREFIX)
public class SecHubSecurityProperties {
    static final String PREFIX = "sechub.security";

    private static final Logger LOG = LoggerFactory.getLogger(SecHubSecurityProperties.class);
    private static final String ERR_MSG_FORMAT = "The property '%s.%s' must not be null";
    private static final String OAUTH2_MODE = "oauth2";
    private static final String CLASSIC_MODE = "classic";
    private static final Set<String> ALLOWED_MODES = Set.of(OAUTH2_MODE, CLASSIC_MODE);

    /**
     * Holds all the configuration properties for the server to authenticate
     * incoming requests. Authentication can be handled either in 'oauth2' mode or
     * 'classic' mode. Set this to null if none of these modes is needed (e.g. when
     * testing)
     */
    private final ResourceServerProperties server;

    /**
     * Configures the server to offer login functionality for users. With this
     * configuration, the server will be able to provide authentication to users.
     * Set this to null if the server should not offer login functionality.
     */
    private final LoginProperties login;

    private final EncryptionProperties encryption;

    @ConstructorBinding
    public SecHubSecurityProperties(ResourceServerProperties server, LoginProperties login, EncryptionProperties encryption) {
        this.server = server;
        if (server == null) {
            LOG.warn("The property '%s.server' is not set".formatted(PREFIX));
        }
        this.login = login;
        this.encryption = login != null && login.isEnabled() ? requireNonNull(encryption, ERR_MSG_FORMAT.formatted(PREFIX, "encryption")) : encryption;
    }

    public ResourceServerProperties getResourceServerProperties() {
        return server;
    }

    public LoginProperties getLoginProperties() {
        return login;
    }

    public EncryptionProperties getEncryptionProperties() {
        return encryption;
    }

    public static class ResourceServerProperties {
        public static final String MODES = "modes";
        public static final String OAUTH2 = "oauth2";
        public static final String CLASSIC = "classic";
        static final String PREFIX = "%s.server".formatted(SecHubSecurityProperties.PREFIX);

        private final Set<String> modes;
        private final OAuth2Properties oAuth2;

        @ConstructorBinding
        public ResourceServerProperties(Set<String> modes, OAuth2Properties oAuth2) {
            this.modes = requireNonNull(modes, ERR_MSG_FORMAT.formatted(PREFIX, "modes"));
            if (this.modes.isEmpty()) {
                throw new IllegalArgumentException("The property '%s.modes' must at least include 'oauth2' or 'classic' mode".formatted(PREFIX));
            }
            if (this.modes.stream().noneMatch(ALLOWED_MODES::contains)) {
                throw new IllegalArgumentException("The property '%s.modes' allows only 'oauth2' or 'classic' mode".formatted(PREFIX));
            }
            this.oAuth2 = oAuth2;
        }

        public Set<String> getModes() {
            return modes;
        }

        public boolean isOAuth2ModeEnabled() {
            return modes.contains(OAUTH2_MODE);
        }

        public boolean isClassicModeEnabled() {
            return modes.contains(CLASSIC_MODE);
        }

        public OAuth2Properties getOAuth2Properties() {
            return oAuth2;
        }

        public static class OAuth2Properties {
            public static final String MODE = "mode";
            public static final String OAUTH2_JWT_MODE = "jwt";
            public static final String OAUTH2_OPAQUE_TOKEN_MODE = "opaque-token";
            static final String PREFIX = "%s.oauth2".formatted(ResourceServerProperties.PREFIX);
            private static final Set<String> ALLOWED_MODES = Set.of(OAUTH2_JWT_MODE, OAUTH2_OPAQUE_TOKEN_MODE);

            private final String mode;
            private final JwtProperties jwt;
            private final OpaqueTokenProperties opaqueToken;

            @ConstructorBinding
            public OAuth2Properties(String mode, JwtProperties jwt, OpaqueTokenProperties opaqueToken) {
                this.mode = requireNonNull(mode, ERR_MSG_FORMAT.formatted(PREFIX, "mode"));
                if (!ALLOWED_MODES.contains(mode)) {
                    throw new IllegalArgumentException("The property '%s.mode' allows only 'jwt' or 'opaque-token' mode".formatted(PREFIX));
                }
                this.jwt = OAUTH2_JWT_MODE.equals(this.mode) ? requireNonNull(jwt, ERR_MSG_FORMAT.formatted(PREFIX, "jwt")) : null;
                this.opaqueToken = OAUTH2_OPAQUE_TOKEN_MODE.equals(this.mode) ? requireNonNull(opaqueToken, ERR_MSG_FORMAT.formatted(PREFIX, "opaque-token"))
                        : null;
            }

            public String getMode() {
                return mode;
            }

            public boolean isJwtModeEnabled() {
                return OAUTH2_JWT_MODE.equals(mode);
            }

            public boolean isOpaqueTokenModeEnabled() {
                return OAUTH2_OPAQUE_TOKEN_MODE.equals(mode);
            }

            public JwtProperties getJwtProperties() {
                return jwt;
            }

            public OpaqueTokenProperties getOpaqueTokenProperties() {
                return opaqueToken;
            }

            public static class JwtProperties {
                static final String PREFIX = "%s.jwt".formatted(OAuth2Properties.PREFIX);

                private final String jwkSetUri;

                @ConstructorBinding
                public JwtProperties(String jwkSetUri) {
                    this.jwkSetUri = requireNonNull(jwkSetUri, ERR_MSG_FORMAT.formatted(PREFIX, "jwk-set-uri"));
                }

                public String getJwkSetUri() {
                    return jwkSetUri;
                }
            }

            public static class OpaqueTokenProperties {
                static final String PREFIX = "%s.opaque-token".formatted(OAuth2Properties.PREFIX);

                private final String introspectionUri;
                private final String clientId;
                private final String clientSecret;
                private final Duration defaultTokenExpiresIn;
                private final Duration maxCacheDuration;

                @ConstructorBinding
                /* @formatter:off */
                public OpaqueTokenProperties(String introspectionUri,
                                             String clientId,
                                             String clientSecret,
                                             Duration defaultTokenExpiresIn,
                                             Duration maxCacheDuration) {
                    /* @formatter:on */
                    this.introspectionUri = requireNonNull(introspectionUri, ERR_MSG_FORMAT.formatted(PREFIX, "introspection-uri"));
                    this.clientId = requireNonNull(clientId, ERR_MSG_FORMAT.formatted(PREFIX, "client-id"));
                    this.clientSecret = requireNonNull(clientSecret, ERR_MSG_FORMAT.formatted(PREFIX, "client-secret"));
                    this.defaultTokenExpiresIn = defaultTokenExpiresIn == null ? Duration.ofDays(1) : defaultTokenExpiresIn;
                    this.maxCacheDuration = requireNonNull(maxCacheDuration, ERR_MSG_FORMAT.formatted(PREFIX, "max-cache-duration"));
                }

                public String getIntrospectionUri() {
                    return introspectionUri;
                }

                public String getClientId() {
                    return clientId;
                }

                public String getClientSecret() {
                    return clientSecret;
                }

                public Duration getDefaultTokenExpiresAt() {
                    return defaultTokenExpiresIn;
                }

                public Duration getMaxCacheDuration() {
                    return maxCacheDuration;
                }
            }
        }
    }

    public static class LoginProperties {
        static final String PREFIX = "%s.login".formatted(SecHubSecurityProperties.PREFIX);
        static final String CLASSIC_MODE = "classic";
        static final String OAUTH2_MODE = "oauth2";
        static final String MODES = "modes";

        private final boolean isEnabled;
        private final String loginPage;
        private final String redirectUri;
        private final Set<String> modes;
        private final OAuth2Properties oAuth2;
        private final ClassicAuthProperties classicAuth;

        /* @formatter:off */
        public LoginProperties(Boolean enabled,
                               String loginPage,
                               String redirectUri,
                               Set<String> modes,
                               OAuth2Properties oAuth2,
                               ClassicAuthProperties classicAuth) {
            /* @formatter:on */
            this.isEnabled = requireNonNull(enabled, ERR_MSG_FORMAT.formatted(PREFIX, "enabled"));
            this.loginPage = enabled ? requireNonNull(loginPage, ERR_MSG_FORMAT.formatted(PREFIX, "login-page")) : loginPage;
            this.redirectUri = enabled ? requireNonNull(redirectUri, ERR_MSG_FORMAT.formatted(PREFIX, "redirect-uri")) : redirectUri;
            this.modes = enabled ? requireNonNull(modes, ERR_MSG_FORMAT.formatted(PREFIX, "modes")) : modes;
            if (enabled && this.modes.isEmpty()) {
                throw new IllegalArgumentException("The property '%s.modes' must at least include 'oauth2' or 'classic' mode".formatted(PREFIX));
            }
            if (enabled && this.modes.stream().noneMatch(ALLOWED_MODES::contains)) {
                throw new IllegalArgumentException("The property '%s.modes' allows only 'oauth2' or 'classic' mode".formatted(PREFIX));
            }
            this.oAuth2 = enabled && isOAuth2ModeEnabled() ? requireNonNull(oAuth2, ERR_MSG_FORMAT.formatted(PREFIX, "oauth2")) : oAuth2;

            if (enabled && isClassicModeEnabled()) {
                this.classicAuth = requireNonNullElseGet(classicAuth, ClassicAuthProperties::new);
            } else {
                this.classicAuth = classicAuth;
            }
        }

        public boolean isEnabled() {
            return isEnabled;
        }

        public String getLoginPage() {
            return loginPage;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public Set<String> getModes() {
            return modes;
        }

        public boolean isOAuth2ModeEnabled() {
            return modes.contains(OAUTH2_MODE);
        }

        public boolean isClassicModeEnabled() {
            return modes.contains(SecHubSecurityProperties.CLASSIC_MODE);
        }

        public OAuth2Properties getOAuth2Properties() {
            return oAuth2;
        }

        public ClassicAuthProperties getClassicAuthProperties() {
            return classicAuth;
        }

        public static class OAuth2Properties {
            static final String PREFIX = "%s.oauth2".formatted(LoginProperties.PREFIX);

            private final String clientId;
            private final String clientSecret;
            private final String provider;
            private final String redirectUri;
            private final String issuerUri;
            private final String authorizationUri;
            private final String tokenUri;
            private final String userInfoUri;
            private final String jwkSetUri;

            @ConstructorBinding
            public OAuth2Properties(String clientId, String clientSecret, String provider, String redirectUri, String issuerUri, String authorizationUri,
                    String tokenUri, String userInfoUri, String jwkSetUri) {
                this.clientId = requireNonNull(clientId, ERR_MSG_FORMAT.formatted(PREFIX, "client-id"));
                this.clientSecret = requireNonNull(clientSecret, ERR_MSG_FORMAT.formatted(PREFIX, "client-secret"));
                this.provider = requireNonNull(provider, ERR_MSG_FORMAT.formatted(PREFIX, "provider"));
                this.redirectUri = requireNonNull(redirectUri, ERR_MSG_FORMAT.formatted(PREFIX, "redirect-uri"));
                this.issuerUri = requireNonNull(issuerUri, ERR_MSG_FORMAT.formatted(PREFIX, "issuer-uri"));
                this.authorizationUri = requireNonNull(authorizationUri, ERR_MSG_FORMAT.formatted(PREFIX, "authorization-uri"));
                this.tokenUri = requireNonNull(tokenUri, ERR_MSG_FORMAT.formatted(PREFIX, "token-uri"));
                this.userInfoUri = requireNonNull(userInfoUri, ERR_MSG_FORMAT.formatted(PREFIX, "user-info-uri"));
                this.jwkSetUri = requireNonNull(jwkSetUri, ERR_MSG_FORMAT.formatted(PREFIX, "jwk-set-uri"));
            }

            public String getClientId() {
                return clientId;
            }

            public String getClientSecret() {
                return clientSecret;
            }

            public String getProvider() {
                return provider;
            }

            public String getRedirectUri() {
                return redirectUri;
            }

            public String getIssuerUri() {
                return issuerUri;
            }

            public String getAuthorizationUri() {
                return authorizationUri;
            }

            public String getTokenUri() {
                return tokenUri;
            }

            public String getUserInfoUri() {
                return userInfoUri;
            }

            public String getJwkSetUri() {
                return jwkSetUri;
            }

        }

        public static class ClassicAuthProperties {
            static final String PREFIX = "%s.classic".formatted(LoginProperties.PREFIX);

            private static final Duration COOKIE_AGE_DEFAULT = Duration.ofHours(24);
            private final Duration cookieAge;

            public ClassicAuthProperties() {
                this.cookieAge = COOKIE_AGE_DEFAULT;
            }

            @ConstructorBinding
            public ClassicAuthProperties(Long cookieAgeSeconds) {
                if (cookieAgeSeconds == null) {
                    this.cookieAge = COOKIE_AGE_DEFAULT;
                } else {
                    this.cookieAge = Duration.ofSeconds(cookieAgeSeconds);
                }
            }

            public Duration getCookieAge() {
                return cookieAge;
            }

            public long getCookieAgeSeconds() {
                return cookieAge.getSeconds();
            }
        }
    }

    public static class EncryptionProperties {
        static final String PREFIX = "%s.encryption".formatted(SecHubSecurityProperties.PREFIX);
        private static final int AES_256_SECRET_KEY_LENGTH = 32;

        private final SealedObject secretKey;

        @ConstructorBinding
        public EncryptionProperties(String secretKey) {
            requireNonNull(secretKey, ERR_MSG_FORMAT.formatted(PREFIX, "secret-key"));
            if (!is256BitString(secretKey)) {
                throw new IllegalArgumentException("The property %s.%s must be a 256-bit string".formatted(PREFIX, "secret-key"));
            }
            this.secretKey = CryptoAccess.CRYPTO_STRING.seal(secretKey);
        }

        public String getSecretKey() {
            return CryptoAccess.CRYPTO_STRING.unseal(secretKey);
        }

        /*
         * Checks if the secret key length is 32 characters (32 * 8 = 256 bits)
         */
        private static boolean is256BitString(String secretKey) {
            return secretKey.length() == AES_256_SECRET_KEY_LENGTH;
        }
    }
    /* @formatter:on */
}
