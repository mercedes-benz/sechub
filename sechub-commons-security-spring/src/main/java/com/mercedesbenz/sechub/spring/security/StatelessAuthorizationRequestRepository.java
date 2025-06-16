package com.mercedesbenz.sechub.spring.security;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class StatelessAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final Logger logger = LoggerFactory.getLogger(StatelessAuthorizationRequestRepository.class);
    private static final String COOKIE_NAME = "SECHUB_OAUTH2_AUTHORIZATION_REQUEST";
    private static final Duration COOKIE_DURATION = Duration.ofMinutes(1);
    /* @formatter:off */
    private static final ObjectMapper mapper = new ObjectMapper()
            .addMixIn(OAuth2AuthorizationRequest.class, OAuth2AuthorizationRequestMixin.class)
            .addMixIn(OAuth2AuthorizationResponseType.class, OAuth2AuthorizationRequestMixin.OAuth2AuthorizationResponseTypeMixin.class)
            .addMixIn(AuthorizationGrantType.class, OAuth2AuthorizationRequestMixin.AuthorizationGrantTypeMixin.class);
    /* @formatter:on */
    private static final Base64.Encoder b64Encoder = Base64.getEncoder();
    private static final Base64.Decoder b64Decoder = Base64.getDecoder();

    private final AES256Encryption aes256Encryption;

    StatelessAuthorizationRequestRepository(AES256Encryption aes256Encryption) {
        this.aes256Encryption = requireNonNull(aes256Encryption, "Property 'aes256Encryption' must not be null");
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return getOAuth2AuthorizationRequestFromCookies(request).orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        String authorizationRequestString;

        try {
            authorizationRequestString = mapper.writeValueAsString(authorizationRequest);
        } catch (JsonProcessingException e) {
            logger.error("Could not serialize authorization request:", e);
            throw new InternalAuthenticationServiceException("Could not serialize authorization request:", e);
        }

        byte[] authorizationRequestStringEncrypted = aes256Encryption.encrypt(authorizationRequestString);

        String authorizationRequestStringB64 = b64Encoder.encodeToString(authorizationRequestStringEncrypted);

        Cookie cookie = CookieHelper.createCookie(COOKIE_NAME, authorizationRequestStringB64, COOKIE_DURATION, AbstractSecurityConfiguration.BASE_PATH);

        logger.trace("Saving authorization request cookie to cookie: {}", cookie);

        response.addCookie(cookie);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        Optional<OAuth2AuthorizationRequest> optOAuthAuthorizationRequest = getOAuth2AuthorizationRequestFromCookies(request);

        if (optOAuthAuthorizationRequest.isPresent()) {
            logger.trace("Removing authorization request from cookies: {}", optOAuthAuthorizationRequest.get());
            CookieHelper.removeCookie(response, COOKIE_NAME);
            return optOAuthAuthorizationRequest.get();
        }

        logger.trace("No authorization request found to remove. Returning null.");

        return null;
    }

    private Optional<OAuth2AuthorizationRequest> getOAuth2AuthorizationRequestFromCookies(HttpServletRequest request) {
        /* @formatter:off */
        Optional<OAuth2AuthorizationRequest> optOAuth2AuthorizationRequest = CookieHelper.getCookie(request, COOKIE_NAME)
                .map(Cookie::getValue)
                .map(b64Decoder::decode)
                .map(aes256Encryption::decrypt)
                .map(StatelessAuthorizationRequestRepository::parseOAuth2AuthorizationRequestFromString);
        /* @formatter:on */

        if (optOAuth2AuthorizationRequest.isPresent()) {
            logger.trace("Successfully loaded authorization request from cookies with name: {}", COOKIE_NAME);
        } else {
            logger.trace("No authorization request found in cookies.");
        }

        return optOAuth2AuthorizationRequest;
    }

    private static OAuth2AuthorizationRequest parseOAuth2AuthorizationRequestFromString(String value) {
        try {
            logger.info("Hallo Laura. Das hier sollte mixins verwenden");
            return mapper.readValue(value, OAuth2AuthorizationRequest.class);
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize OAuth2AuthorizationRequest from value: {}", value, e);
            throw new InternalAuthenticationServiceException("Failed to deserialize OAuth2AuthorizationRequest", e);
        }
    }

    private abstract static class OAuth2AuthorizationRequestMixin {

        private static final String AUTHORIZATION_URI = "authorizationUri";
        private static final String RESPONSE_TYPE = "responseType";
        private static final String CLIENT_ID = "clientId";
        private static final String REDIRECT_URI = "redirectUri";
        private static final String SCOPES = "scopes";
        private static final String STATE = "state";
        private static final String ADDITIONAL_PARAMETERS = "additionalParameters";
        private static final String AUTHORIZATION_REQUEST_URI = "authorizationRequestUri";
        private static final String ATTRIBUTES = "attributes";
        private static final String GRANT_TYPE = "grantType";

        private final String authorizationUri;
        private final OAuth2AuthorizationResponseType responseType;
        private final String clientId;
        private final String redirectUri;
        private final Set<String> scopes;
        private final String state;
        private final Map<String, Object> additionalParameters;
        private final String authorizationRequestUri;
        private final Map<String, Object> attributes;
        private final AuthorizationGrantType authorizationGrantType;

        /* @formatter:off */
        @JsonCreator
        private OAuth2AuthorizationRequestMixin(@JsonProperty(AUTHORIZATION_URI) String authorizationUri,
                                                @JsonProperty(RESPONSE_TYPE) OAuth2AuthorizationResponseType responseType,
                                                @JsonProperty(CLIENT_ID) String clientId,
                                                @JsonProperty(REDIRECT_URI) String redirectUri,
                                                @JsonProperty(SCOPES) Set<String> scopes,
                                                @JsonProperty(STATE) String state,
                                                @JsonProperty(ADDITIONAL_PARAMETERS) Map<String, Object> additionalParameters,
                                                @JsonProperty(AUTHORIZATION_REQUEST_URI) String authorizationRequestUri,
                                                @JsonProperty(ATTRIBUTES) Map<String, Object> attributes,
                                                @JsonProperty(GRANT_TYPE) AuthorizationGrantType authorizationGrantType) {
            /* @formatter:on */
            this.authorizationUri = authorizationUri;
            this.responseType = responseType;
            this.clientId = clientId;
            this.redirectUri = redirectUri;
            this.scopes = scopes;
            this.state = state;
            this.additionalParameters = additionalParameters;
            this.authorizationRequestUri = authorizationRequestUri;
            this.attributes = attributes;
            this.authorizationGrantType = authorizationGrantType;
        }

        public String getAuthorizationUri() {
            return authorizationUri;
        }

        public OAuth2AuthorizationResponseType getResponseType() {
            return responseType;
        }

        public String getClientId() {
            return clientId;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public Set<String> getScopes() {
            return scopes;
        }

        public String getState() {
            return state;
        }

        public Map<String, Object> getAdditionalParameters() {
            return additionalParameters;
        }

        public String getAuthorizationRequestUri() {
            return authorizationRequestUri;
        }

        public Map<String, Object> getAttributes() {
            return attributes;
        }

        public AuthorizationGrantType getGrantType() {
            return authorizationGrantType;
        }

        private abstract static class OAuth2AuthorizationResponseTypeMixin {
            private static final String VALUE = "value";

            private final String value;

            @JsonCreator
            private OAuth2AuthorizationResponseTypeMixin(@JsonProperty(VALUE) String value) {
                this.value = value;
            }

            public String getValue() {
                return value;
            }
        }

        private abstract static class AuthorizationGrantTypeMixin {
            private static final String VALUE = "value";

            private final String value;

            @JsonCreator
            private AuthorizationGrantTypeMixin(@JsonProperty(VALUE) String value) {
                this.value = value;
            }

            public String getValue() {
                return value;
            }
        }

    }
}