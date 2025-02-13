// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SealedObject;

import com.mercedesbenz.sechub.commons.core.cache.InMemoryCache;
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

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

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
    private static final InMemoryCache<OAuth2OpaqueTokenIntrospectionResponse> cache = new InMemoryCache<>(null);

    private final RestTemplate restTemplate;
    private final String introspectionUri;
    private final SealedObject clientIdSealed;
    private final SealedObject clientSecretSealed;
    private final Duration maxCacheDuration;
    private final UserDetailsService userDetailsService;

    /* @formatter:off */
    OAuth2OpaqueTokenIntrospector(RestTemplate restTemplate,
                                  String introspectionUri,
                                  String clientId,
                                  String clientSecret,
                                  Duration maxCacheDuration,
                                  UserDetailsService userDetailsService) {
        this.restTemplate = requireNonNull(restTemplate, "Parameter restTemplate must not be null");
        this.introspectionUri = requireNonNull(introspectionUri, "Parameter introspectionUri must not be null");
        this.clientIdSealed = cryptoString.seal(requireNonNull(clientId, "Parameter clientId must not be null"));
        this.clientSecretSealed = cryptoString.seal(requireNonNull(clientSecret, "Parameter clientSecret must not be null"));
        this.maxCacheDuration = requireNonNull(maxCacheDuration, "Parameter maxCacheDuration must not be null");
        this.userDetailsService = requireNonNull(userDetailsService, "Parameter userDetailsService must not be null");
    }
    /* @formatter:on */

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String opaqueToken) throws OAuth2AuthenticationException {
        if (opaqueToken == null || opaqueToken.isEmpty()) {
            throw new BadOpaqueTokenException("Token is null or empty");
        }

        OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = getIntrospectionResponse(opaqueToken);

        cacheIntrospectionResponse(opaqueToken, introspectionResponse);

        if (!introspectionResponse.isActive()) {
            throw new BadOpaqueTokenException("Token is not active");
        }

        String subject = introspectionResponse.getSubject();

        if (subject == null || subject.isEmpty()) {
            throw new BadOpaqueTokenException("Subject is null");
        }

        Map<String, Object> introspectionClaims = getIntrospectionClaims(introspectionResponse);
        UserDetails userDetails = userDetailsService.loadUserByUsername(subject);
        Collection<GrantedAuthority> authorities = new ArrayList<>(userDetails.getAuthorities());
        return new OAuth2IntrospectionAuthenticatedPrincipal(subject, introspectionClaims, authorities);
    }

    private void cacheIntrospectionResponse(String opaqueToken, OAuth2OpaqueTokenIntrospectionResponse introspectionResponse) {
        Duration cacheDuration = Duration.between(introspectionResponse.getIssuedAt(), introspectionResponse.getExpiresAt());
        if (maxCacheDuration.compareTo(cacheDuration) < 0) {
            log.debug("Opaque token cache duration exceeds the maximum cache duration. Using the maximum cache duration instead.");
            cacheDuration = maxCacheDuration;
        }
        cache.put(opaqueToken, introspectionResponse, cacheDuration);
    }

    private HttpEntity<MultiValueMap<String, String>> buildHttpEntity(String opaqueToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.AUTHORIZATION, getBasicAuthHeaderValue());
        return getRequestParameters(opaqueToken, headers);
    }

    private OAuth2OpaqueTokenIntrospectionResponse getIntrospectionResponse (String opaqueToken) {
        /* @formatter:off */
        return cache
                .get(opaqueToken)
                .orElseGet(() -> fetchTokenIntrospectionResponse(buildHttpEntity(opaqueToken)));
        /* @formatter:on */
    }

    private static HttpEntity<MultiValueMap<String, String>> getRequestParameters(String opaqueToken, HttpHeaders headers) {
        MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
        formParameters.add(TOKEN, opaqueToken);
        formParameters.add(TOKEN_TYPE_HINT, TOKEN_TYPE_HINT_VALUE);
        return new HttpEntity<>(formParameters, headers);
    }

    private OAuth2OpaqueTokenIntrospectionResponse fetchTokenIntrospectionResponse(HttpEntity<MultiValueMap<String, String>> entity) {
        try {
            OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = restTemplate.postForObject(introspectionUri, entity, OAuth2OpaqueTokenIntrospectionResponse.class);

            if (introspectionResponse == null) {
                throw new RestClientException("Response is null");
            }

            return introspectionResponse;
        } catch (RestClientException e) {
            String errMsg = "Failed to perform token introspection";
            log.error(errMsg, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errMsg, e);
        }
    }

    private String getBasicAuthHeaderValue() {
        String clientId = cryptoString.unseal(clientIdSealed);
        String clientSecret = cryptoString.unseal(clientSecretSealed);
        String clientIdClientSecret = CLIENT_ID_CLIENT_SECRET_FORMAT.formatted(clientId, clientSecret);
        String clientIdClientSecretB64Encoded = Base64.getEncoder().encodeToString(clientIdClientSecret.getBytes());
        return BASIC_AUTHORIZATION_HEADER_VALUE_FORMAT.formatted(clientIdClientSecretB64Encoded);
    }

    /* @formatter:off */
    private static Map<String, Object> getIntrospectionClaims(OAuth2OpaqueTokenIntrospectionResponse oAuth2OpaqueTokenIntrospectionResponse) {
        Map<String, Object> map = new HashMap<>();
        map.put(OAuth2TokenIntrospectionClaimNames.ACTIVE, oAuth2OpaqueTokenIntrospectionResponse.isActive());
        map.put(OAuth2TokenIntrospectionClaimNames.SCOPE, oAuth2OpaqueTokenIntrospectionResponse.getScope());
        map.put(OAuth2TokenIntrospectionClaimNames.CLIENT_ID, oAuth2OpaqueTokenIntrospectionResponse.getClientId());
        map.put(OAuth2TokenIntrospectionClaimNames.USERNAME, oAuth2OpaqueTokenIntrospectionResponse.getUsername());
        map.put(OAuth2TokenIntrospectionClaimNames.TOKEN_TYPE, oAuth2OpaqueTokenIntrospectionResponse.getTokenType());
        map.put(OAuth2TokenIntrospectionClaimNames.IAT, oAuth2OpaqueTokenIntrospectionResponse.getIssuedAt());
        map.put(OAuth2TokenIntrospectionClaimNames.EXP, oAuth2OpaqueTokenIntrospectionResponse.getExpiresAt());
        map.put(OAuth2TokenIntrospectionClaimNames.SUB, oAuth2OpaqueTokenIntrospectionResponse.getSubject());
        map.put(OAuth2TokenIntrospectionClaimNames.AUD, oAuth2OpaqueTokenIntrospectionResponse.getAudience());
        return map;
    }
    /* @formatter:on */
}