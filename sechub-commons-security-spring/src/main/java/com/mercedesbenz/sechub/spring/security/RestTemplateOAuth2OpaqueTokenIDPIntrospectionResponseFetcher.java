// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static java.util.Objects.*;

import java.util.Base64;

import javax.crypto.SealedObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

public class RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcher implements OAuth2OpaqueTokenIDPIntrospectionResponseFetcher {

    private static final String TOKEN = "token";
    private static final String BASIC_AUTHORIZATION_HEADER_VALUE_FORMAT = "Basic %s";
    private static final String CLIENT_ID_CLIENT_SECRET_FORMAT = "%s:%s";
    private static final String TOKEN_TYPE_HINT = "token_type_hint";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String TOKEN_TYPE_HINT_VALUE = ACCESS_TOKEN;

    private RestTemplate restTemplate;
    private String introspectionUri;
    private SealedObject clientIdSealed;
    private SealedObject clientSecretSealed;

    private static final Logger logger = LoggerFactory.getLogger(RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcher.class);
    private static final CryptoAccess<String> cryptoAccess = CryptoAccess.CRYPTO_STRING;

    public static RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcherBuilder builder() {
        return new RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcherBuilder();
    }

    public static class RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcherBuilder {

        private RestTemplate restTemplate;
        private String introspectionUri;
        private SealedObject clientIdSealed;
        private SealedObject clientSecretSealed;

        private RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcherBuilder() {

        }

        public RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcherBuilder setRestTemplate(RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
            return this;
        }

        public RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcherBuilder setIntrospectionUri(String introspectionUri) {
            this.introspectionUri = introspectionUri;
            return this;
        }

        public RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcherBuilder setClientId(String clientId) {
            requireNonNull(clientId, "Parameter clientId must not be null");
            this.clientIdSealed = cryptoAccess.seal(clientId);
            return this;
        }

        public RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcherBuilder setClientSecret(String clientSecret) {
            requireNonNull(clientSecret, "Parameter clientSecret must not be null");
            this.clientSecretSealed = cryptoAccess.seal(clientSecret);
            return this;
        }

        public RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcher build() {

            RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcher fetcher = new RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcher();

            fetcher.restTemplate = requireNonNull(restTemplate, "Parameter restTemplate must not be null");
            fetcher.introspectionUri = requireNonNull(introspectionUri, "Parameter introspectionUri must not be null");
            fetcher.clientIdSealed = requireNonNull(clientIdSealed, "Parameter clientId must not be null");
            fetcher.clientSecretSealed = requireNonNull(clientSecretSealed, "Parameter clientSecret must not be null");

            return fetcher;
        }

    }

    private RestTemplateOAuth2OpaqueTokenIDPIntrospectionResponseFetcher() {
    }

    @Override
    public OAuth2OpaqueTokenIntrospectionResponse fetchOpaqueTokenIntrospectionFromIDP(String opaqueToken) {
        if (opaqueToken == null || opaqueToken.isBlank()) {
            throw new BadOpaqueTokenException("Token is null or empty");
        }
        HttpEntity<MultiValueMap<String, String>> httpEntity = buildHttpEntity(opaqueToken);

        try {
            OAuth2OpaqueTokenIntrospectionResponse introspectionResponse = restTemplate.postForObject(introspectionUri, httpEntity,
                    OAuth2OpaqueTokenIntrospectionResponse.class);

            if (introspectionResponse == null) {
                throw new RestClientException("Response is null");
            }

            return introspectionResponse;

        } catch (RestClientException e) {
            String errMsg = "Failed to perform token introspection";
            logger.error(errMsg, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errMsg, e);
        }
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
        String clientId = cryptoAccess.unseal(clientIdSealed);
        String clientSecret = cryptoAccess.unseal(clientSecretSealed);
        String clientIdClientSecret = CLIENT_ID_CLIENT_SECRET_FORMAT.formatted(clientId, clientSecret);
        String clientIdClientSecretB64Encoded = Base64.getEncoder().encodeToString(clientIdClientSecret.getBytes());
        return BASIC_AUTHORIZATION_HEADER_VALUE_FORMAT.formatted(clientIdClientSecretB64Encoded);
    }

}