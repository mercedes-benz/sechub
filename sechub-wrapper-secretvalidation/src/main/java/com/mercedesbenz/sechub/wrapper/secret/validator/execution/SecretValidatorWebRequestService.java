// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorRequest;
import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorRequestHeader;
import com.mercedesbenz.sechub.wrapper.secret.validator.support.SecretValidatorHttpClientWrapper;

@Service
public class SecretValidatorWebRequestService {

    private static final Logger LOG = LoggerFactory.getLogger(SecretValidatorWebRequestService.class);

    private SecretValidatorHttpClientWrapper httpClientWrapper;

    private ResponseValidationService responseValidationService;

    public SecretValidatorWebRequestService(ResponseValidationService responseValidationService, SecretValidatorHttpClientWrapper httpClientWrapper) {

        this.responseValidationService = responseValidationService;
        this.httpClientWrapper = httpClientWrapper;
    }

    public SecretValidationResult validateFinding(String snippetText, String ruleId, List<SecretValidatorRequest> requests, int maximumRetries) {
        SecretValidationResult validationResult = assertValidParams(snippetText, requests);
        if (validationResult != null) {
            return validationResult;
        }

        int failedRequests = 0;
        for (SecretValidatorRequest configuredRequest : requests) {
            HttpResponse<String> response = null;

            if (isRequestValid(configuredRequest)) {
                HttpRequest httpRequest = createHttpRequest(snippetText, configuredRequest);
                response = executeHttpRequest(configuredRequest, httpRequest);

                if (response == null) {
                    response = retryConnection(maximumRetries, configuredRequest, httpRequest);
                }

                if (responseValidationService.isValidResponse(response, configuredRequest.getExpectedResponse())) {
                    LOG.info("Finding of type: {} is valid!", ruleId);
                    return createValidationResult(SecretValidationStatus.VALID, configuredRequest.getUrl());
                }
            }
            if (response == null) {
                failedRequests++;
            }
        }
        // all requests failed
        if (failedRequests == requests.size()) {
            LOG.warn("All requests for finding of type: {} seem to have failed", ruleId);
            return createValidationResult(SecretValidationStatus.ALL_VALIDATION_REQUESTS_FAILED);
        }
        return createValidationResult(SecretValidationStatus.INVALID);
    }

    private HttpResponse<String> retryConnection(int maximumRetries, SecretValidatorRequest configuredRequest, HttpRequest httpRequest) {
        HttpResponse<String> response = null;
        for (int retries = 0; retries < maximumRetries; retries++) {
            response = executeHttpRequest(configuredRequest, httpRequest);
            if (response != null) {
                return response;
            }
        }
        return response;
    }

    private SecretValidationResult assertValidParams(String snippetText, List<SecretValidatorRequest> requests) {
        if (snippetText == null || snippetText.isBlank()) {
            LOG.warn("Cannot validate finding because the SARIF snippet text is null or empty.");
            return createValidationResult(SecretValidationStatus.SARIF_SNIPPET_NOT_SET);
        }

        if (requests.isEmpty()) {
            LOG.info("Configured requests for this finding empty! Finding cannot be validated!");
            return createValidationResult(SecretValidationStatus.NO_VALIDATION_CONFIGURED);
        }
        return null;
    }

    private boolean isRequestValid(SecretValidatorRequest request) {
        if (request == null) {
            LOG.info("Request config is null! Entry will be skipped.");
            return false;
        }
        if (request.getUrl() == null) {
            LOG.info("Request config URL is null! Entry will be skipped.");
            return false;
        }
        return true;
    }

    private HttpResponse<String> executeHttpRequest(SecretValidatorRequest configuredRequest, HttpRequest httpRequest) {
        boolean proxyRequired = configuredRequest.isProxyRequired();
        boolean verifyCertificate = configuredRequest.isVerifyCertificate();
        if (proxyRequired) {
            if (verifyCertificate) {
                return httpClientWrapper.sendProxiedRequestVerifyCertificate(httpRequest);
            }
            return httpClientWrapper.sendProxiedRequestIgnoreCertificate(httpRequest);

        } else {
            if (verifyCertificate) {
                return httpClientWrapper.sendDirectRequestVerifyCertificate(httpRequest);
            }
            return httpClientWrapper.sendDirectRequestIgnoreCertificate(httpRequest);
        }
    }

    private HttpRequest createHttpRequest(String snippetText, SecretValidatorRequest request) {
        List<String> headers = new ArrayList<>();
        for (SecretValidatorRequestHeader header : request.getHeaders()) {
            String value = snippetText;
            if (header.getValuePrefix() != null) {
                value = header.getValuePrefix() + " " + snippetText;
            }
            headers.add(header.getName());
            headers.add(value);
        }
        String[] headersAsArrays = new String[headers.size()];
        try {
            /* @formatter:off */
            return HttpRequest.newBuilder()
                        .headers(headers.toArray(headersAsArrays))
                        .uri(request.getUrl().toURI())
                        .GET()
                        .build();
            /* @formatter:on */
        } catch (URISyntaxException e) {
            LOG.error("Request URL {} is invalid!", request.getUrl());
            throw new IllegalArgumentException(e);
        }
    }

    private SecretValidationResult createValidationResult(SecretValidationStatus validationStatus) {
        return createValidationResult(validationStatus, null);
    }

    private SecretValidationResult createValidationResult(SecretValidationStatus validationStatus, URL secretWasValidFor) {
        SecretValidationResult validationResult = new SecretValidationResult();
        validationResult.setValidationStatus(validationStatus);
        if (secretWasValidFor != null) {
            validationResult.setValidatedByUrl(secretWasValidFor.toString());
        }
        return validationResult;
    }

}
