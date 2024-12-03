// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import javax.crypto.SealedObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

@ConfigurationProperties(prefix = SecurityProperties.PREFIX)
public class SecurityProperties {
    static final String PREFIX = "sechub.security";

    private static final Logger LOG = LoggerFactory.getLogger(SecurityProperties.class);
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
    private final Server server;

    /**
     * Configures the server to offer login functionality for users. With this
     * configuration, the server will be able to provide authentication to users.
     * Set this to null if the server should not offer login functionality.
     */
    private final Login login;

    private final Encryption encryption;

    @ConstructorBinding
    public SecurityProperties(Server server, Login login, Encryption encryption) {
        this.server = server;
        if (server == null) {
            LOG.warn("The property '%s.server' is not set. The server will not be able to authenticate requests".formatted(PREFIX));
        }
        this.login = login;
        this.encryption = login != null && login.isEnabled() ? requireNonNull(encryption, ERR_MSG_FORMAT.formatted(PREFIX, "encryption")) : encryption;
    }

    public Server getServer() {
        return server;
    }

    public Login getLogin() {
        return login;
    }

    public Encryption getEncryption() {
        return encryption;
    }

    public static class Server {
        static final String PREFIX = "%s.server".formatted(SecurityProperties.PREFIX);

        private final Set<String> modes;
        private final OAuth2 oAuth2;

        @ConstructorBinding
        public Server(Set<String> modes, OAuth2 oAuth2) {
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

        public OAuth2 getOAuth2() {
            return oAuth2;
        }

        public static class OAuth2 {
            static final String PREFIX = "%s.oauth2".formatted(Server.PREFIX);
            public static final String OAUTH2_JWT_MODE = "jwt";
            public static final String OAUTH2_OPAQUE_TOKEN_MODE = "opaque-token";
            private static final Set<String> ALLOWED_MODES = Set.of(OAUTH2_JWT_MODE, OAUTH2_OPAQUE_TOKEN_MODE);

            private final String mode;
            private final Jwt jwt;
            private final OpaqueToken opaqueToken;

            @ConstructorBinding
            public OAuth2(String mode, Jwt jwt, OpaqueToken opaqueToken) {
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

            public Jwt getJwt() {
                return jwt;
            }

            public OpaqueToken getOpaqueToken() {
                return opaqueToken;
            }

            public static class Jwt {
                static final String PREFIX = "%s.jwt".formatted(OAuth2.PREFIX);

                private final String jwkSetUri;

                @ConstructorBinding
                public Jwt(String jwkSetUri) {
                    this.jwkSetUri = requireNonNull(jwkSetUri, ERR_MSG_FORMAT.formatted(PREFIX, "jwk-set-uri"));
                }

                public String getJwkSetUri() {
                    return jwkSetUri;
                }
            }

            public static class OpaqueToken {
                static final String PREFIX = "%s.opaque-token".formatted(OAuth2.PREFIX);

                private final String introspectionUri;
                private final String clientId;
                private final String clientSecret;

                @ConstructorBinding
                public OpaqueToken(String introspectionUri, String clientId, String clientSecret) {
                    this.introspectionUri = requireNonNull(introspectionUri, ERR_MSG_FORMAT.formatted(PREFIX, "introspection-uri"));
                    this.clientId = requireNonNull(clientId, ERR_MSG_FORMAT.formatted(PREFIX, "client-id"));
                    this.clientSecret = requireNonNull(clientSecret, ERR_MSG_FORMAT.formatted(PREFIX, "client-secret"));
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
            }
        }
    }

    public static class Login {
        static final String PREFIX = "%s.login".formatted(SecurityProperties.PREFIX);

        private final boolean isEnabled;
        private final String loginPage;
        private final String redirectUri;
        private final Set<String> modes;
        private final OAuth2 oAuth2;

        public Login(Boolean enabled, String loginPage, String redirectUri, Set<String> modes, OAuth2 oAuth2) {
            this.isEnabled = requireNonNull(enabled, ERR_MSG_FORMAT.formatted(PREFIX, "enabled"));
            this.loginPage = requireNonNull(loginPage, ERR_MSG_FORMAT.formatted(PREFIX, "login-page"));
            this.redirectUri = requireNonNull(redirectUri, ERR_MSG_FORMAT.formatted(PREFIX, "redirect-uri"));
            this.modes = requireNonNull(modes, ERR_MSG_FORMAT.formatted(PREFIX, "modes"));
            if (this.modes.isEmpty()) {
                throw new IllegalArgumentException("The property '%s.modes' must at least include 'oauth2' or 'classic' mode".formatted(PREFIX));
            }
            if (this.modes.stream().noneMatch(ALLOWED_MODES::contains)) {
                throw new IllegalArgumentException("The property '%s.modes' allows only 'oauth2' or 'classic' mode".formatted(PREFIX));
            }
            /*
             * Later we will differentiate between classic and oauth2 login. For now only
             * oauth2 login is enabled
             */
            this.oAuth2 = requireNonNull(oAuth2, ERR_MSG_FORMAT.formatted(PREFIX, "oauth2"));
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
            return modes.contains(CLASSIC_MODE);
        }

        public OAuth2 getOAuth2() {
            return oAuth2;
        }

        public static class OAuth2 {
            static final String PREFIX = "%s.oauth2".formatted(Login.PREFIX);

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
            public OAuth2(String clientId, String clientSecret, String provider, String redirectUri, String issuerUri, String authorizationUri, String tokenUri,
                    String userInfoUri, String jwkSetUri) {
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
    }

    public static class Encryption {
        static final String PREFIX = "%s.encryption".formatted(SecurityProperties.PREFIX);
        private static final int AES_256_SECRET_KEY_LENGTH = 32;

        private final SealedObject secretKey;

        @ConstructorBinding
        public Encryption(String secretKey) {
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
