// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorRequest;
import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorRequestHeader;
import com.mercedesbenz.sechub.wrapper.secret.validator.support.SecretValidatorHttpClientWrapper;

class SecretValidatorWebRequestServiceTest {

    private SecretValidatorWebRequestService serviceTotest;

    private static final SecretValidatorHttpClientWrapper httpClientWrapper = mock();
    private static final ResponseValidationService responseValidationService = mock();

    @BeforeEach
    void beforeEach() {
        Mockito.reset(responseValidationService, httpClientWrapper);
        serviceTotest = new SecretValidatorWebRequestService(responseValidationService, httpClientWrapper);
    }

    @Test
    void no_finding_snippet_text_available_results_in_finding_being_skipped_from_validation() {
        /* execute */
        SecretValidationResult validationResult = serviceTotest.validateFinding(null, "example-rule-id", new ArrayList<>(), 0L);

        /* test */
        assertEquals(SecretValidationStatus.SARIF_SNIPPET_NOT_SET, validationResult.getValidationStatus());
    }

    @Test
    void no_requests_defined_results_in_finding_being_skipped_from_validation() {
        /* execute */
        SecretValidationResult validationResult = serviceTotest.validateFinding("not-empty", "example-rule-id", new ArrayList<>(), 0L);

        /* test */
        assertEquals(SecretValidationStatus.NO_VALIDATION_CONFIGURED, validationResult.getValidationStatus());
    }

    @Test
    void request_url_is_null_results_request_will_be_skipped() {
        /* prepare */
        ArrayList<SecretValidatorRequest> requests = new ArrayList<>();
        requests.add(new SecretValidatorRequest());

        /* execute */
        SecretValidationResult validationResult = serviceTotest.validateFinding("no-empty", "example-rule-id", requests, 0L);

        /* test */
        // no validation request was performed ends up with the following status, only
        // if at least 1 request was performed, the finding could be marked as invalid.
        assertEquals(SecretValidationStatus.ALL_VALIDATION_REQUESTS_FAILED, validationResult.getValidationStatus());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void proxy_required_calls_the_correct_http_client_returns_expected_validation_result(boolean expectedValidation) throws IOException, InterruptedException {
        /* prepare */
        long connectionRetries = 2L;

        List<SecretValidatorRequest> requests = createListOfRequests(true);
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(httpClientWrapper.sendProxiedRequestVerifyCertificate(any())).thenReturn(response);
        when(responseValidationService.isValidResponse(any(), any())).thenReturn(expectedValidation);

        /* execute */
        SecretValidationResult validationResult = serviceTotest.validateFinding("no-empty", "example-rule-id", requests, connectionRetries);

        /* test */
        if (expectedValidation) {
            assertEquals(SecretValidationStatus.VALID, validationResult.getValidationStatus());
            assertEquals("http://example.com", validationResult.getValidatedByUrl());
        } else {
            assertEquals(SecretValidationStatus.INVALID, validationResult.getValidationStatus());
        }

        verify(httpClientWrapper, times(3)).sendProxiedRequestVerifyCertificate(any());

        verify(httpClientWrapper, never()).sendProxiedRequestIgnoreCertificate(any());
        verify(httpClientWrapper, never()).sendDirectRequestVerifyCertificate(any());
        verify(httpClientWrapper, never()).sendDirectRequestIgnoreCertificate(any());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void no_proxy_required_calls_the_correct_http_client_results_validation_result(boolean expectedValidation) throws IOException, InterruptedException {
        /* prepare */
        long connectionRetries = 3L;
        List<SecretValidatorRequest> requests = createListOfRequests(false);

        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(httpClientWrapper.sendDirectRequestVerifyCertificate(any())).thenReturn(response);
        when(responseValidationService.isValidResponse(any(), any())).thenReturn(expectedValidation);

        /* execute */
        SecretValidationResult validationResult = serviceTotest.validateFinding("no-empty", "example-rule-id", requests, connectionRetries);

        /* test */
        if (expectedValidation) {
            assertEquals(SecretValidationStatus.VALID, validationResult.getValidationStatus());
            assertEquals("http://example.com", validationResult.getValidatedByUrl());
        } else {
            assertEquals(SecretValidationStatus.INVALID, validationResult.getValidationStatus());
        }

        verify(httpClientWrapper, times(4)).sendDirectRequestVerifyCertificate(any());

        verify(httpClientWrapper, never()).sendProxiedRequestVerifyCertificate(any());
        verify(httpClientWrapper, never()).sendProxiedRequestIgnoreCertificate(any());
        verify(httpClientWrapper, never()).sendDirectRequestIgnoreCertificate(any());
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
