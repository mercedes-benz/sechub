// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import java.util.Base64;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

/**
 * <p>
 * Custom implementation of {@link OAuth2AccessTokenResponseClient} for
 * retrieving a JWT token response from a configured Identity Provider (IDP)
 * after a successful OAuth2 authorization code grant request.
 * </p>
 * <p>
 * This class handles the exchange of the authorization code for an access
 * token, refresh token, and ID token by making a POST request to the token
 * endpoint of the IDP. The client credentials (client ID and client secret) are
 * encoded in Base64 and included in the Authorization header of the request.
 * </p>
 * <p>
 * The response from the IDP is expected to be a {@link JwtResponse}, which
 * includes the access token, refresh token, ID token, and the expiration time
 * of the access token.
 * </p>
 *
 * @see OAuth2AccessTokenResponseClient
 * @see OAuth2AuthorizationCodeGrantRequest
 * @see OAuth2AccessTokenResponse
 * @see SecurityConfiguration
 * @see JwtResponse
 *
 * @author hamidonos
 */
class Base64EncodedClientIdAndSecretOAuth2AccessTokenClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(Base64EncodedClientIdAndSecretOAuth2AccessTokenClient.class);
    private static final String GRANT_TYPE_VALUE = "authorization_code";
    private static final String BASIC_AUTHORIZATION_HEADER_VALUE_FORMAT = "Basic %s";
    private static final String CLIENT_ID_CLIENT_SECRET_FORMAT = "%s:%s";
    private static final String ID_TOKEN = "id_token";

    private final RestTemplate restTemplate;

    Base64EncodedClientIdAndSecretOAuth2AccessTokenClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
        ClientRegistration clientRegistration = authorizationGrantRequest.getClientRegistration();
        String tokenUri = clientRegistration.getProviderDetails().getTokenUri();
        String clientId = clientRegistration.getClientId();
        String clientSecret = clientRegistration.getClientSecret();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.AUTHORIZATION, getBasicAuthHeaderValue(clientId, clientSecret));

        HttpEntity<MultiValueMap<String, String>> entity = getMultiValueMapHttpEntity(authorizationGrantRequest, headers);

        JwtResponse jwtResponse;
        try {
            jwtResponse = restTemplate.postForObject(tokenUri, entity, JwtResponse.class);

            if (jwtResponse == null) {
                throw new RestClientException("JWT response is null");
            }
        } catch (RestClientException e) {
            String errMsg = "Failed to get JWT token response";
            LOG.error(errMsg, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errMsg, e);
        }

        Map<String, Object> additionalParameters = Map.of(ID_TOKEN, jwtResponse.getIdToken());

        // @formatter:off
        return OAuth2AccessTokenResponse
                .withToken(jwtResponse.getAccessToken())
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .expiresIn(jwtResponse.getExpiresIn())
                .refreshToken(jwtResponse.getRefreshToken())
                .additionalParameters(additionalParameters)
                .build();
        // @formatter:on
    }

    private static HttpEntity<MultiValueMap<String, String>> getMultiValueMapHttpEntity(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest,
            HttpHeaders headers) {
        MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
        OAuth2AuthorizationExchange authorizationExchange = authorizationGrantRequest.getAuthorizationExchange();
        String code = authorizationExchange.getAuthorizationResponse().getCode();
        String redirectUri = authorizationExchange.getAuthorizationRequest().getRedirectUri();

        formParameters.add(OAuth2ParameterNames.GRANT_TYPE, GRANT_TYPE_VALUE);
        formParameters.add(OAuth2ParameterNames.CODE, code);
        formParameters.add(OAuth2ParameterNames.REDIRECT_URI, redirectUri);

        return new HttpEntity<>(formParameters, headers);
    }

    private static String getBasicAuthHeaderValue(String clientId, String clientSecret) {
        String clientIdClientSecret = CLIENT_ID_CLIENT_SECRET_FORMAT.formatted(clientId, clientSecret);
        String clientIdClientSecretB64Encoded = Base64.getEncoder().encodeToString(clientIdClientSecret.getBytes());
        return BASIC_AUTHORIZATION_HEADER_VALUE_FORMAT.formatted(clientIdClientSecretB64Encoded);
    }
}