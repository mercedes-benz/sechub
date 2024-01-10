// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

@OnlyForRegularTestExecution
class JsonErrorFieldNamesAvailableHttpStatusExceptionTestValidatorTest {

    @Test
    void expected_404_all_as_expected_in_json_is_valid() {
        /* prepare */
        String json = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Project s02_0046project1 does not exist, or you have no access.\",\"details\":[],\"timeStamp\":\"Wed Apr 06 06:47:20 CEST 2022\"}";
        HttpStatus expectedStatus = HttpStatus.NOT_FOUND;

        JsonErrorFieldNamesAvailableHttpStatusExceptionTestValidator validatorToTest = new JsonErrorFieldNamesAvailableHttpStatusExceptionTestValidator(
                expectedStatus);

        HttpStatusCodeException exception = createMockedException(json, expectedStatus);

        /* execute + test (no exception ) */
        validatorToTest.validate(exception);

    }

    @Test
    void expected_404_is_containing_all_json_content_but_status_in_json_is_401_is_invalid() {
        /* prepare */
        String json = "{\"status\":401,\"error\":\"Not Found\",\"message\":\"Project s02_0046project1 does not exist, or you have no access.\",\"details\":[],\"timeStamp\":\"Wed Apr 06 06:47:20 CEST 2022\"}";
        HttpStatus expectedStatus = HttpStatus.NOT_FOUND;

        JsonErrorFieldNamesAvailableHttpStatusExceptionTestValidator validatorToTest = new JsonErrorFieldNamesAvailableHttpStatusExceptionTestValidator(
                expectedStatus);

        HttpStatusCodeException exception = createMockedException(json, expectedStatus);

        /* execute + test */
        assertThrows(AssertionError.class, () -> validatorToTest.validate(exception));

    }

    @Test
    void expected_404_is_containing_all_json_content_but_error_empty_is_invalid() {
        /* prepare */
        String json = "{\"status\":404,\"error\":\"\",\"message\":\"Project s02_0046project1 does not exist, or you have no access.\",\"details\":[],\"timeStamp\":\"Wed Apr 06 06:47:20 CEST 2022\"}";
        HttpStatus expectedStatus = HttpStatus.NOT_FOUND;

        JsonErrorFieldNamesAvailableHttpStatusExceptionTestValidator validatorToTest = new JsonErrorFieldNamesAvailableHttpStatusExceptionTestValidator(
                expectedStatus);

        HttpStatusCodeException exception = createMockedException(json, expectedStatus);

        /* execute + test */
        assertThrows(AssertionError.class, () -> validatorToTest.validate(exception));

    }

    @Test
    void expected_404_is_containing_all_json_content_but_message_empty_is_invalid() {
        /* prepare */
        String json = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"\",\"details\":[],\"timeStamp\":\"Wed Apr 06 06:47:20 CEST 2022\"}";
        HttpStatus expectedStatus = HttpStatus.NOT_FOUND;

        JsonErrorFieldNamesAvailableHttpStatusExceptionTestValidator validatorToTest = new JsonErrorFieldNamesAvailableHttpStatusExceptionTestValidator(
                expectedStatus);

        HttpStatusCodeException exception = createMockedException(json, expectedStatus);

        /* execute + test */
        assertThrows(AssertionError.class, () -> validatorToTest.validate(exception));

    }

    @Test
    void expected_404_is_containing_all_json_content_but_error_is_not_as_expected_is_invalid() {
        /* prepare */
        String json = "{\"status\":404,\"error\":\"Not (changed) Found\",\"message\":\"xxx\",\"details\":[],\"timeStamp\":\"Wed Apr 06 06:47:20 CEST 2022\"}";
        HttpStatus expectedStatus = HttpStatus.NOT_FOUND;

        JsonErrorFieldNamesAvailableHttpStatusExceptionTestValidator validatorToTest = new JsonErrorFieldNamesAvailableHttpStatusExceptionTestValidator(
                expectedStatus);

        HttpStatusCodeException exception = createMockedException(json, expectedStatus);

        /* execute + test */
        assertThrows(AssertionError.class, () -> validatorToTest.validate(exception));

    }

    @Test
    void expected_404_is_containing_all_json_content_but_timestamp_empty_is_invalid() {
        /* prepare */
        String json = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Project s02_0046project1 does not exist, or you have no access.\",\"details\":[],\"timeStamp\":\"\"}";
        HttpStatus expectedStatus = HttpStatus.NOT_FOUND;

        JsonErrorFieldNamesAvailableHttpStatusExceptionTestValidator validatorToTest = new JsonErrorFieldNamesAvailableHttpStatusExceptionTestValidator(
                expectedStatus);

        HttpStatusCodeException exception = createMockedException(json, expectedStatus);

        /* execute + test */
        assertThrows(AssertionError.class, () -> validatorToTest.validate(exception));

    }

    @Test
    void expected_404_empty_body_is_invalid() {
        /* prepare */
        String json = "";
        HttpStatus expectedStatus = HttpStatus.NOT_FOUND;

        JsonErrorFieldNamesAvailableHttpStatusExceptionTestValidator validatorToTest = new JsonErrorFieldNamesAvailableHttpStatusExceptionTestValidator(
                expectedStatus);

        HttpStatusCodeException exception = createMockedException(json, expectedStatus);

        /* execute + test */
        assertThrows(AssertionError.class, () -> validatorToTest.validate(exception));

    }

    @Test
    void expected_404_null_body_is_invalid() {
        /* prepare */
        String json = null;
        HttpStatus expectedStatus = HttpStatus.NOT_FOUND;

        JsonErrorFieldNamesAvailableHttpStatusExceptionTestValidator validatorToTest = new JsonErrorFieldNamesAvailableHttpStatusExceptionTestValidator(
                expectedStatus);

        HttpStatusCodeException exception = createMockedException(json, expectedStatus);

        /* execute + test */
        assertThrows(AssertionError.class, () -> validatorToTest.validate(exception));

    }

    private HttpStatusCodeException createMockedException(String json, HttpStatus expectedStatus) {
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(expectedStatus);
        when(exception.getResponseBodyAsString()).thenReturn(json);
        return exception;
    }

}
