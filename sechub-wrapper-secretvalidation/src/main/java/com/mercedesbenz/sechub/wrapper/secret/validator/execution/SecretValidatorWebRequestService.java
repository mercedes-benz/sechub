// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorRequest;
import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorRequestHeader;
import com.mercedesbenz.sechub.wrapper.secret.validator.support.SecretValidatorHttpClientFactory;

@Service
public class SecretValidatorWebRequestService {

    private static final Logger LOG = LoggerFactory.getLogger(SecretValidatorWebRequestService.class);

    @Autowired
    SecretValidatorHttpClientFactory httpClientFactory;

    @Autowired
    ResponseValidationService responseValidationService;

    public SecretValidationResult validateFinding(String snippetText, List<SecretValidatorRequest> requests, boolean trustAllCertificates) {
        SecretValidationResult validationResult = assertValidParams(snippetText, requests);
        if (validationResult != null) {
            return validationResult;
        }

        HttpClient proxyHttpClient = httpClientFactory.createProxyHttpClient(trustAllCertificates);
        HttpClient directHttpClient = httpClientFactory.createDirectHttpClient(trustAllCertificates);
        HttpResponse<String> response = null;
        for (SecretValidatorRequest request : requests) {

            if (isRequestValid(request)) {
                response = createAndExecuteHttpRequest(snippetText, proxyHttpClient, directHttpClient, request);

                if (responseValidationService.isValidResponse(response, request.getExpectedResponse())) {
                    LOG.info("Finding is valid!");
                    return createValidationResult(SecretValidationStatus.VALID, request.getUrl());
                }
            }
        }
        if (response == null) {
            return createValidationResult(SecretValidationStatus.ALL_VALIDATION_REQUESTS_FAILED);
        }
        return createValidationResult(SecretValidationStatus.INVALID);
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

    private HttpResponse<String> createAndExecuteHttpRequest(String snippetText, HttpClient proxyHttpClient, HttpClient directHttpClient,
            SecretValidatorRequest request) {
        HttpResponse<String> response = null;
        try {
            HttpRequest httpRequest = createHttpRequest(snippetText, request);
            if (request.isProxyRequired()) {
                response = proxyHttpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            } else {
                response = directHttpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            }
        } catch (IOException | InterruptedException e) {
            LOG.error("Performing validation request failed!", e);
        }
        return response;
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
