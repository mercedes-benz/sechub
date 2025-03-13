// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static java.util.Objects.*;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.crypto.SealedObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import com.mercedesbenz.sechub.commons.core.doc.Description;
import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils;

@MustBeDocumented(scope = "Login and resource server")
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

    private static Set<String> requireNonEmpy(Set<String> set, String message) {
        if (set == null || set.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return set;
    }

    /* @formatter:off */
    @ConstructorBinding
    public SecHubSecurityProperties(
            ResourceServerProperties server,

            LoginProperties login,

            EncryptionProperties encryption) {
        /* @formatter:on */
        this.server = server;
        if (server == null) {
            LOG.warn("The property '%s.server' is not set".formatted(PREFIX));
        }
        this.login = login;
        this.encryption = login != null ? requireNonNull(encryption, ERR_MSG_FORMAT.formatted(PREFIX, "encryption")) : encryption;
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
        static final String PREFIX = SecHubSecurityProperties.PREFIX + ".server";

        private final Set<String> modes;
        private final OAuth2Properties oAuth2;

        @ConstructorBinding
        /* @formatter:off */
        public ResourceServerProperties(

                @Description("The server modes to use as a comma separated list. Possible values are '"+OAUTH2_MODE+"' and '"+CLASSIC_MODE+"'")
                String modes,

                OAuth2Properties oAuth2
                ) {
            /* @formatter:on */
            Set<String> modesParameterSet = new LinkedHashSet<>(SimpleStringUtils.createListForCommaSeparatedValues(modes));
            this.modes = requireNonEmpy(modesParameterSet, "The property '%s.modes' must at least include 'oauth2' or 'classic' mode".formatted(PREFIX));

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
            static final String PREFIX = ResourceServerProperties.PREFIX + ".oauth2";
            private static final Set<String> ALLOWED_MODES = Set.of(OAUTH2_JWT_MODE, OAUTH2_OPAQUE_TOKEN_MODE);

            private final String mode;
            private final JwtProperties jwt;
            private final OpaqueTokenProperties opaqueToken;

            @ConstructorBinding
            /* @formatter:off */
            public OAuth2Properties(
                    @Description("The oauth2 mode to use. Can be either '"+OAUTH2_JWT_MODE+"' or '"+OAUTH2_OPAQUE_TOKEN_MODE+"'")
                    String mode,

                    JwtProperties jwt,

                    OpaqueTokenProperties opaqueToken

                    ) {
                /* @formatter:on */
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
                static final String PREFIX = OAuth2Properties.PREFIX + ".jwt";

                private final String jwkSetUri;

                @ConstructorBinding
                /* @formatter:off */
                public JwtProperties(
                        @Description("URI for jwk. For example: https://idp.example.com/oauth2/v3/certs")
                        String jwkSetUri) {
                    /* @formatter:on */
                    this.jwkSetUri = requireNonNull(jwkSetUri, ERR_MSG_FORMAT.formatted(PREFIX, "jwk-set-uri"));
                }

                public String getJwkSetUri() {
                    return jwkSetUri;
                }
            }

            public static class OpaqueTokenProperties {
                static final String PREFIX = OAuth2Properties.PREFIX + ".opaque-token";

                private final String introspectionUri;
                private final String clientId;
                private final String clientSecret;
                private final Duration defaultTokenExpiresIn;
                private final Duration maxCacheDuration;

                @ConstructorBinding
                /* @formatter:off */
                public OpaqueTokenProperties(
                                 @Description("Introspection URI of the identify provider, will be used to check if the given opaque token from login is valid.")
                                 String introspectionUri,

                                 @Description("Client id for oauth2 client being used for opaque token handling")
                                 String clientId,

                                 @Description("The secret for the oauth2 client being used for opaque token handling")
                                 String clientSecret,

                                 @Description("The default token expiration time. Is used as fallback when IDP does not provide an expiration time. Uses standard java duration syntax. For example '60m' means sixty minutes, '1d' means one day.")
                                 Duration defaultTokenExpiresIn,

                                 @Description("The maximum cache duration. To avoid that the IDP is always asked again about the validity of an opaquetoken, the acceptance is cached. When this time exceeds,the introspection will be done and cached again. Uses standard java duration syntax. For example '60m' means sixty minutes, '1d' means one day.")
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
        static final String PREFIX = SecHubSecurityProperties.PREFIX + ".login";
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
        public LoginProperties(@Description("Defines if login enabled or not")
                               Boolean enabled,

                               @Description("The login page which can be used by external client (like WebUI)")
                               String loginPage,

                               @Description("The redirect URI after a succesful login is done")
                               String redirectUri,

                               @Description("The login modes to use as a comma separated list. Possible values are '"+OAUTH2_MODE+"' and '"+CLASSIC_MODE+"'")
                               String modes,

                               @Description("Configuration for oauth2, only relevant when login modes contain '"+OAUTH2_MODE+"'")
                               OAuth2Properties oAuth2,

                               @Description("Configuration for classic mode, only relevant when login modes contain '"+CLASSIC_MODE+"'")
                               ClassicAuthProperties classicAuth) {
            /* @formatter:on */
            this.isEnabled = requireNonNull(enabled, ERR_MSG_FORMAT.formatted(PREFIX, "enabled"));
            this.loginPage = enabled ? requireNonNull(loginPage, ERR_MSG_FORMAT.formatted(PREFIX, "login-page")) : loginPage;

            this.redirectUri = enabled ? requireNonNull(redirectUri, ERR_MSG_FORMAT.formatted(PREFIX, "redirect-uri")) : redirectUri;

            Set<String> modesParameterSet = new LinkedHashSet<>(SimpleStringUtils.createListForCommaSeparatedValues(modes));
            this.modes = enabled ? requireNonEmpy(modesParameterSet, "The property '%s.%s' must not be empty or null".formatted(PREFIX, MODES))
                    : modesParameterSet;

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
            static final String PREFIX = LoginProperties.PREFIX + ".oauth2";

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
            /* @formatter:off */
            public OAuth2Properties(
                    @Description("The client id used for oauth2 login handling")
                    String clientId,

                    @Description("The client secret used for oauth2 login handling")
                    String clientSecret,

                    @Description("Name of the oauth2 provider. For example 'keycloak'")
                    String provider,

                    @Description("This is the callback URI where the IDP will redirect the user after successful login; 'https://<sechub-server-host>/login/oauth2/code/<provider>'. For most IDPs this URI has to be configured inside the IDP client")
                    String redirectUri,

                    @Description("URI that identifies the issuer. For example: https://idp.example.org")
                    String issuerUri,

                    @Description("URI that identifies the Authorization Server. For example: https://idp.example.org/oauth2/v2/auth")
                    String authorizationUri,

                    @Description("Represents the URI for the token endpoint. For example: https://idp.example.org/oauth2/v4/token")
                    String tokenUri,

                    @Description("URI for user information. For example: https://idp.example.org/oauth2/v3/userinfo")
                    String userInfoUri,

                    @Description("URI for jwk. For example: https://idp.example.org/oauth2/v3/certs")
                    String jwkSetUri) {

                /* @formatter:on */
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
            static final String PREFIX = LoginProperties.PREFIX + ".classic";

            private static final Duration COOKIE_AGE_DEFAULT = Duration.ofHours(24);
            private final Duration cookieAge;

            public ClassicAuthProperties() {
                this.cookieAge = COOKIE_AGE_DEFAULT;
            }

            @ConstructorBinding
            /* @formatter:off */
            public ClassicAuthProperties(
                    @Description("Cookie age in seconds.")
                    Long cookieAgeSeconds) {
                /* @formatter:on */
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
        static final String PREFIX = SecHubSecurityProperties.PREFIX + ".encryption";
        private static final int AES_256_SECRET_KEY_LENGTH = 32;

        private final SealedObject secretKey;

        @ConstructorBinding
        /* @formatter:off */
        public EncryptionProperties(
                @Description("The secret key for encryption (used for cookies etc.). Must be an exactly 256 bit long string")
                String secretKey
                ) {
            /* @formatter:on */
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
