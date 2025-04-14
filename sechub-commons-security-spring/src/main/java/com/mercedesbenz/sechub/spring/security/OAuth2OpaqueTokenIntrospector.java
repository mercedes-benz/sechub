// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.crypto.SealedObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.mercedesbenz.sechub.commons.core.cache.InMemoryCache;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.core.shutdown.ApplicationShutdownHandler;

/**
 * <p>
 * The <code>Base64OAuth2OpaqueTokenIntrospector</code> class is responsible for
 * introspecting opaque OAuth2 tokens. It sends the opaque token to an
 * introspection endpoint and retrieves the token's details.
 * </p>
 *
 * <p>
 * This class integrates with the {@link UserDetailsService} to load user
 * details based on the token's subject. The user details are then used to
 * create an {@link OAuth2IntrospectionAuthenticatedPrincipal} which contains
 * the authenticated principal's information and authorities.
 * </p>
 *
 * <p>
 * The class also handles the encoding of client credentials using Base64 and
 * includes them in the <code>Authorization</code> header of the introspection
 * request.
 * </p>
 *
 * @see org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector
 * @see org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
 * @see org.springframework.security.core.userdetails.UserDetailsService
 * @see org.springframework.web.client.RestTemplate
 *
 * @author hamidonos
 */
class OAuth2OpaqueTokenIntrospector implements OpaqueTokenIntrospector {

    private static final Logger log = LoggerFactory.getLogger(OAuth2OpaqueTokenIntrospector.class);
    private static final CryptoAccess<String> cryptoString = CryptoAccess.CRYPTO_STRING;
    private static final String TOKEN = "token";
    private static final String BASIC_AUTHORIZATION_HEADER_VALUE_FORMAT = "Basic %s";
    private static final String CLIENT_ID_CLIENT_SECRET_FORMAT = "%s:%s";
    private static final String TOKEN_TYPE_HINT = "token_type_hint";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String TOKEN_TYPE_HINT_VALUE = ACCESS_TOKEN;

    private final RestTemplate restTemplate;
    private final String introspectionUri;
    private final SealedObject clientIdSealed;
    private final SealedObject clientSecretSealed;
    private final Duration defaultTokenExpiresIn;
    private final Duration maxCacheDuration;
    private final UserDetailsService userDetailsService;
    private final InMemoryCache<OAuth2OpaqueTokenIntrospectionResponse> cache;
    private OAuth2OpaqueTokenExpirationCalculator expirationCalculator;

    /* @formatter:off */
    OAuth2OpaqueTokenIntrospector(RestTemplate restTemplate,
                                  String introspectionUri,
                                  String clientId,
                                  String clientSecret,
                                  Duration defaultTokenExpiresIn,
                                  Duration maxCacheDuration,
                                  UserDetailsService userDetailsService,
                                  ApplicationShutdownHandler applicationShutdownHandler,
                                  OAuth2OpaqueTokenExpirationCalculator expirationCalculator) {

        this.restTemplate = requireNonNull(restTemplate, "Parameter restTemplate must not be null");
        this.introspectionUri = requireNonNull(introspectionUri, "Parameter introspectionUri must not be null");
        this.clientIdSealed = cryptoString.seal(requireNonNull(clientId, "Parameter clientId must not be null"));
        this.clientSecretSealed = cryptoString.seal(requireNonNull(clientSecret, "Parameter clientSecret must not be null"));
        this.defaultTokenExpiresIn = requireNonNull(defaultTokenExpiresIn, "Parameter defaultTokenExpiresIn must not be null");
        this.maxCacheDuration = requireNonNull(maxCacheDuration, "Parameter maxCacheDuration must not be null");
        this.userDetailsService = requireNonNull(userDetailsService, "Parameter userDetailsService must not be null");
        this.expirationCalculator=requireNonNull(expirationCalculator);

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.cache = new InMemoryCache<>(maxCacheDuration, scheduledExecutorService, applicationShutdownHandler);
    }
    /* @formatter:on */

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String opaqueToken) throws OAuth2AuthenticationException {
        if (opaqueToken == null || opaqueToken.isEmpty()) {
            throw new BadOpaqueTokenException("Token is null or empty");
        }

        Instant now = Instant.now();

        OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = getIntrospectionResponse(opaqueToken, now);

        if (expirationCalculator.isExpired(introspectionResponse, now)) {
            /*
             * Remove value from cache (we differ between expiration and cache time and want
             * no orphans in cache).
             */
            cache.remove(opaqueToken);

            /*
             * Remark: Spring Boot will catch any BadOpaqueTokenException inside
             * OpaqueTokenAuthenticationProvider and transform it to an
             * InvalidBearerTokenException which will be fetched and handled by our custom
             * Authentication entry point inside AbstractSecurityConfiguration, leading to a
             * remove of the oauth2 cookie at browser side and finally to a 401 unauthorized
             * response.
             */
            throw new BadOpaqueTokenException("Opaque token is expired");
        }

        if (!introspectionResponse.isActive()) {
            throw new BadOpaqueTokenException("Token is not active");
        }

        String subject = introspectionResponse.getSubject();

        if (subject == null || subject.isEmpty()) {
            throw new BadOpaqueTokenException("Subject is null or empty");
        }

        Map<String, Object> introspectionClaims = getIntrospectionClaims(introspectionResponse, now);
        UserDetails userDetails = userDetailsService.loadUserByUsername(subject);
        Collection<GrantedAuthority> authorities = new ArrayList<>(userDetails.getAuthorities());
        String username = userDetails.getUsername();

        return new OAuth2IntrospectionAuthenticatedPrincipal(username, introspectionClaims, authorities);
    }

    private OAuth2OpaqueTokenIntrospectionResponse getIntrospectionResponse(String opaqueToken, Instant now) {
        /* @formatter:off */
        return cache
                .get(opaqueToken)
                .orElseGet(() -> fetchAndCacheTokenIntrospectionResponse(opaqueToken, now));
        /* @formatter:on */
    }

    private OAuth2OpaqueTokenIntrospectionResponse fetchAndCacheTokenIntrospectionResponse(String opaqueToken, Instant now) {
        OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = fetchTokenIntrospectionResponse(opaqueToken, now);
        cacheIntrospectionResponse(opaqueToken, introspectionResponse, now);
        return introspectionResponse;
    }

    private OAuth2OpaqueTokenIntrospectionResponse fetchTokenIntrospectionResponse(String opaqueToken, Instant now) {
        HttpEntity<MultiValueMap<String, String>> httpEntity = buildHttpEntity(opaqueToken);

        try {
            OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = restTemplate.postForObject(introspectionUri, httpEntity,
                    OAuth2OpaqueTokenIntrospectionResponse.class);

            if (introspectionResponse == null) {
                throw new RestClientException("Response is null");
            }

            if (introspectionResponse.getExpiresAt() == null) {

                Instant calculatedExpiresAtWithDefault = now.plus(defaultTokenExpiresIn);
                introspectionResponse.setExpiresAt(calculatedExpiresAtWithDefault);

                log.debug("Opaque token introspection response did not contain an `expiresAt` entry! Set `expiresAt` to calculated value `{}` as fallback.",
                        calculatedExpiresAtWithDefault);
            }

            return introspectionResponse;

        } catch (RestClientException e) {
            String errMsg = "Failed to perform token introspection";
            log.error(errMsg, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errMsg, e);
        }
    }

    private void cacheIntrospectionResponse(String opaqueToken, OAuth2OpaqueTokenIntrospectionResponse introspectionResponse, Instant now) {
        Instant cacheExpiresAt;

        // currently we always use the expiresAt from response to define how long the
        // cache value is cached
        cacheExpiresAt = introspectionResponse.getExpiresAt();

        // sanity check
        if (cacheExpiresAt == null) {
            throw new IllegalStateException("May not happen - must be handled with defaults before!");
        }

        Duration cacheDuration = Duration.between(now, cacheExpiresAt);

        if (maxCacheDuration.compareTo(cacheDuration) < 0) {
            log.debug("Opaque token cache duration of %s exceeds the maximum cache duration of %s. Using the maximum cache duration instead."
                    .formatted(cacheDuration.toString(), maxCacheDuration.toString()));
            cacheDuration = maxCacheDuration;
        }

        cache.put(opaqueToken, introspectionResponse, cacheDuration);
    }

    private HttpEntity<MultiValueMap<String, String>> buildHttpEntity(String opaqueToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.AUTHORIZATION, getBasicAuthHeaderValue());
        MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
        formParameters.add(TOKEN, opaqueToken);
        formParameters.add(TOKEN_TYPE_HINT, TOKEN_TYPE_HINT_VALUE);
        return new HttpEntity<>(formParameters, headers);
    }

    private String getBasicAuthHeaderValue() {
        String clientId = cryptoString.unseal(clientIdSealed);
        String clientSecret = cryptoString.unseal(clientSecretSealed);
        String clientIdClientSecret = CLIENT_ID_CLIENT_SECRET_FORMAT.formatted(clientId, clientSecret);
        String clientIdClientSecretB64Encoded = Base64.getEncoder().encodeToString(clientIdClientSecret.getBytes());
        return BASIC_AUTHORIZATION_HEADER_VALUE_FORMAT.formatted(clientIdClientSecretB64Encoded);
    }

    /* @formatter:off */
    private Map<String, Object> getIntrospectionClaims(OAuth2OpaqueTokenIntrospectionResponse oAuth2OpaqueTokenIntrospectionResponse, Instant now) {
        Map<String, Object> map = new HashMap<>();
        map.put(OAuth2TokenIntrospectionClaimNames.ACTIVE, oAuth2OpaqueTokenIntrospectionResponse.isActive());
        map.put(OAuth2TokenIntrospectionClaimNames.SCOPE, oAuth2OpaqueTokenIntrospectionResponse.getScope());
        map.put(OAuth2TokenIntrospectionClaimNames.CLIENT_ID, oAuth2OpaqueTokenIntrospectionResponse.getClientId());
        map.put(OAuth2TokenIntrospectionClaimNames.USERNAME, oAuth2OpaqueTokenIntrospectionResponse.getUsername());
        map.put(OAuth2TokenIntrospectionClaimNames.TOKEN_TYPE, oAuth2OpaqueTokenIntrospectionResponse.getTokenType());
        map.put(OAuth2TokenIntrospectionClaimNames.IAT, oAuth2OpaqueTokenIntrospectionResponse.getIssuedAt());
        Instant expiresAt = oAuth2OpaqueTokenIntrospectionResponse.getExpiresAt();
        if (expiresAt == null) {
            expiresAt = now.plus(defaultTokenExpiresIn);
        }
        map.put(OAuth2TokenIntrospectionClaimNames.EXP, expiresAt);
        map.put(OAuth2TokenIntrospectionClaimNames.SUB, oAuth2OpaqueTokenIntrospectionResponse.getSubject());
        map.put(OAuth2TokenIntrospectionClaimNames.AUD, oAuth2OpaqueTokenIntrospectionResponse.getAudience());
        return map;
    }
    /* @formatter:on */
}