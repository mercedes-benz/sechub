package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorResponse;
import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorResponseContains;

class ResponseValidationServiceTest {

    private ResponseValidationService serviceToTest = new ResponseValidationService();

    @Test
    void response_is_null_results_in_validation_is_false() {
        /* execute */
        boolean isValid = serviceToTest.isValidResponse(null, new SecretValidatorResponse());

        /* test */
        assertFalse(isValid);
    }

    @Test
    void validator_response_config_is_null_results_in_validation_is_false() {
        /* prepare */
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);

        /* execute */
        boolean isValid = serviceToTest.isValidResponse(response, null);

        /* test */
        assertFalse(isValid);
    }

    @ParameterizedTest
    @ValueSource(ints = { 200, 302, 404, 500 })
    void validator_response_config_status_code_configured_and_contains_is_null_results_in_http_status_code_check(int responseCode) {
        /* prepare */
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(responseCode);
        SecretValidatorResponse secretValidatorResponse = new SecretValidatorResponse();
        secretValidatorResponse.setHttpStatus(302);
        secretValidatorResponse.setContains(null);

        // with this configuration we expect the satuscodes being compared,
        // since nothing else is configured inside the SecretValidatorResponse
        boolean expectedResponse = response.statusCode() == secretValidatorResponse.getHttpStatus();

        /* execute */
        boolean isValid = serviceToTest.isValidResponse(response, secretValidatorResponse);

        /* test */
        assertEquals(expectedResponse, isValid);
    }

    @ParameterizedTest
    @ValueSource(ints = { 200, 302, 404, 500 })
    void validator_response_config_status_code_configured_and_contains_is_empty_results_in_http_status_code_check(int responseCode) {
        /* prepare */
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(responseCode);
        SecretValidatorResponse secretValidatorResponse = new SecretValidatorResponse();
        secretValidatorResponse.setHttpStatus(302);
        secretValidatorResponse.setContains(new SecretValidatorResponseContains());

        // with this configuration we expect the satuscodes being compared,
        // since nothing else is configured inside the SecretValidatorResponse
        boolean expectedResponse = response.statusCode() == secretValidatorResponse.getHttpStatus();

        /* execute */
        boolean isValid = serviceToTest.isValidResponse(response, secretValidatorResponse);

        /* test */
        assertEquals(expectedResponse, isValid);
    }

    @Test
    void validator_response_config_status_code_not_configured_and_contains_is_null_results_in_false() {
        /* prepare */
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        SecretValidatorResponse secretValidatorResponse = new SecretValidatorResponse();
        secretValidatorResponse.setContains(null);

        /* execute */
        boolean isValid = serviceToTest.isValidResponse(response, secretValidatorResponse);

        /* test */
        assertFalse(isValid);
    }

    @Test
    void validator_response_config_status_code_not_configured_and_contains_is_empty_results_in_false() {
        /* prepare */
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        SecretValidatorResponse secretValidatorResponse = new SecretValidatorResponse();
        secretValidatorResponse.setContains(new SecretValidatorResponseContains());

        /* execute */
        boolean isValid = serviceToTest.isValidResponse(response, secretValidatorResponse);

        /* test */
        assertFalse(isValid);
    }

    @Test
    void response_body_is_null_with_statuscode_configured_results_in_false() {
        /* prepare */
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(null);
        SecretValidatorResponse responseConfig = createSecretValidatorResponseWithAllOfSetup();
        responseConfig.setHttpStatus(200);

        /* execute */
        boolean isValid = serviceToTest.isValidResponse(response, responseConfig);

        /* test */
        assertFalse(isValid);
    }

    @Test
    void response_body_is_null_without_statuscode_configured_results_in_false() {
        /* prepare */
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(null);
        SecretValidatorResponse responseConfig = createSecretValidatorResponseWithAllOfSetup();

        /* execute */
        boolean isValid = serviceToTest.isValidResponse(response, responseConfig);

        /* test */
        assertFalse(isValid);
    }

    @Test
    void response_body_contains_all_of_expected_substings_returns_true() {
        /* prepare */
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("OK is authorized");
        SecretValidatorResponse responseConfig = createSecretValidatorResponseWithAllOfSetup();

        /* execute */
        boolean isValid = serviceToTest.isValidResponse(response, responseConfig);

        /* test */
        assertTrue(isValid);
    }

    @Test
    void response_body_does_not_contain_all_of_expected_substings_returns_false() {
        /* prepare */
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("OK");
        SecretValidatorResponse responseConfig = createSecretValidatorResponseWithAllOfSetup();

        /* execute */
        boolean isValid = serviceToTest.isValidResponse(response, responseConfig);

        /* test */
        assertFalse(isValid);
    }

    @Test
    void response_body_contains_one_of_expected_substings_returns_true() {
        /* prepare */
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("OK");
        SecretValidatorResponse responseConfig = createSecretValidatorResponseWithOneOfSetup();

        /* execute */
        boolean isValid = serviceToTest.isValidResponse(response, responseConfig);

        /* test */
        assertTrue(isValid);
    }

    @Test
    void response_body_does_not_contain_one_of_expected_substings_returns_false() {
        /* prepare */
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("Authentication successful!");
        SecretValidatorResponse responseConfig = createSecretValidatorResponseWithAllOfSetup();

        /* execute */
        boolean isValid = serviceToTest.isValidResponse(response, responseConfig);

        /* test */
        assertFalse(isValid);
    }

    private SecretValidatorResponse createSecretValidatorResponseWithAllOfSetup() {
        SecretValidatorResponse secretValidatorResponse = new SecretValidatorResponse();
        SecretValidatorResponseContains contains = new SecretValidatorResponseContains();
        List<String> allOf = new ArrayList<>();
        allOf.add("OK");
        allOf.add("authorized");

        contains.setAllOf(allOf);
        secretValidatorResponse.setContains(contains);

        return secretValidatorResponse;
    }

    private SecretValidatorResponse createSecretValidatorResponseWithOneOfSetup() {
        SecretValidatorResponse secretValidatorResponse = new SecretValidatorResponse();
        SecretValidatorResponseContains contains = new SecretValidatorResponseContains();
        List<String> oneOf = new ArrayList<>();
        oneOf.add("OK");
        oneOf.add("authorized");

        contains.setOneOf(oneOf);
        secretValidatorResponse.setContains(contains);

        return secretValidatorResponse;
    }

}
