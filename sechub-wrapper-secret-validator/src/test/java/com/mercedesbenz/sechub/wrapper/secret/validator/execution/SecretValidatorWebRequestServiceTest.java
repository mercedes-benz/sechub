// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorRequest;
import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorRequestHeader;
import com.mercedesbenz.sechub.wrapper.secret.validator.support.SecretValidatorHttpClientFactory;

class SecretValidatorWebRequestServiceTest {

    private SecretValidatorWebRequestService serviceTotest;

    private SecretValidatorHttpClientFactory httpClientFactory;
    private ResponseValidationService responseValidationService;

    @BeforeEach
    void beforeEach() {
        serviceTotest = new SecretValidatorWebRequestService();
        httpClientFactory = new SecretValidatorHttpClientFactory();

        httpClientFactory = mock(SecretValidatorHttpClientFactory.class);
        responseValidationService = mock(ResponseValidationService.class);

        serviceTotest.httpClientFactory = httpClientFactory;
        serviceTotest.responseValidationService = responseValidationService;
    }

    @Test
    void no_finding_snippet_text_available_results_in_finding_being_skipped_from_validation() {
        /* execute */
        SecretValidationResult validationResult = serviceTotest.validateFinding(null, new ArrayList<>(), true);

        /* test */
        assertEquals(SecretValidationStatus.SARIF_SNIPPET_NOT_SET, validationResult.getValidationStatus());
    }

    @Test
    void no_requests_defined_results_in_finding_being_skipped_from_validation() {
        /* execute */
        SecretValidationResult validationResult = serviceTotest.validateFinding("not-empty", new ArrayList<>(), true);

        /* test */
        assertEquals(SecretValidationStatus.NO_VALIDATION_CONFIGURED, validationResult.getValidationStatus());
    }

    @Test
    void request_config_inside_list_is_null_results_request_will_be_skipped() {
        /* prepare */
        ArrayList<SecretValidatorRequest> requests = new ArrayList<>();
        requests.add(null);

        /* execute */
        SecretValidationResult validationResult = serviceTotest.validateFinding("not-empty", requests, true);

        /* test */
        // no validation request was performed ends up with the following status, only
        // if at least 1 request was performed, the finding could be marked as invalid.
        assertEquals(SecretValidationStatus.ALL_VALIDATION_REQUESTS_FAILED, validationResult.getValidationStatus());
    }

    @Test
    void request_url_is_null_results_request_will_be_skipped() {
        /* prepare */
        ArrayList<SecretValidatorRequest> requests = new ArrayList<>();
        requests.add(new SecretValidatorRequest());

        /* execute */
        SecretValidationResult validationResult = serviceTotest.validateFinding("no-empty", requests, true);

        /* test */
        // no validation request was performed ends up with the following status, only
        // if at least 1 request was performed, the finding could be marked as invalid.
        assertEquals(SecretValidationStatus.ALL_VALIDATION_REQUESTS_FAILED, validationResult.getValidationStatus());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void proxy_required_calls_the_correct_http_client_results_validation_result(boolean expectedValidation) throws IOException, InterruptedException {
        /* prepare */
        List<SecretValidatorRequest> requests = createListOfRequests(true);

        HttpClient proxyHttpClient = mock(HttpClient.class);
        when(proxyHttpClient.send(any(), any())).thenReturn(null);

        when(httpClientFactory.createProxyHttpClient(anyBoolean())).thenReturn(proxyHttpClient);

        when(responseValidationService.isValidResponse(any(), any())).thenReturn(expectedValidation);

        /* execute */
        SecretValidationResult validationResult = serviceTotest.validateFinding("no-empty", requests, true);

        /* test */
        if (expectedValidation) {
            assertEquals(SecretValidationStatus.VALID, validationResult.getValidationStatus());
            assertEquals("http://example.com", validationResult.getValidatedByUrl());
        } else {
            assertEquals(SecretValidationStatus.ALL_VALIDATION_REQUESTS_FAILED, validationResult.getValidationStatus());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void no_proxy_required_calls_the_correct_http_client_results_validation_result(boolean expectedValidation) throws IOException, InterruptedException {
        /* prepare */
        List<SecretValidatorRequest> requests = createListOfRequests(false);

        HttpClient directHttpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<Object> response = mock(HttpResponse.class);
        when(directHttpClient.send(any(), any())).thenReturn(response);

        when(httpClientFactory.createDirectHttpClient(anyBoolean())).thenReturn(directHttpClient);

        when(responseValidationService.isValidResponse(any(), any())).thenReturn(expectedValidation);

        /* execute */
        SecretValidationResult validationResult = serviceTotest.validateFinding("no-empty", requests, true);

        /* test */
        if (expectedValidation) {
            assertEquals(SecretValidationStatus.VALID, validationResult.getValidationStatus());
            assertEquals("http://example.com", validationResult.getValidatedByUrl());
        } else {
            assertEquals(SecretValidationStatus.INVALID, validationResult.getValidationStatus());
        }
    }

    private List<SecretValidatorRequest> createListOfRequests(boolean proxyRequired) throws MalformedURLException {
        ArrayList<SecretValidatorRequest> requests = new ArrayList<>();
        SecretValidatorRequest secretValidatorRequest = new SecretValidatorRequest();
        secretValidatorRequest.setProxyRequired(proxyRequired);
        secretValidatorRequest.setUrl(new URL("http://example.com"));
        List<SecretValidatorRequestHeader> headers = new ArrayList<>();
        SecretValidatorRequestHeader header = new SecretValidatorRequestHeader();
        header.setName("Authorization");
        headers.add(header);
        secretValidatorRequest.setHeaders(headers);
        requests.add(secretValidatorRequest);
        return requests;
    }

}
