// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class PDSServerErrorControllerTest {

    private static final String TEST_ERROR_MESSAGE = "my PDS error message";

    private HttpServletResponse response;
    private HttpServletRequest request;
    private PDSServerErrorController controllerToTest;
    private DefaultErrorAttributes errorAttributes;

    @BeforeEach
    void beforeEach() {
        response = mock(HttpServletResponse.class);
        request = mock(HttpServletRequest.class);
        errorAttributes = new DefaultErrorAttributes();

        controllerToTest = new PDSServerErrorController();
        controllerToTest.errorAttributes = errorAttributes;
    }

    @Test
    void using_default_errorattributes_when_response_is_401_the_returns_json_body_contains_the_error_message() {
        /* prepare */
        prepareError(401, TEST_ERROR_MESSAGE);

        /* execute */
        ResponseEntity<PDSServerError> r = controllerToTest.error(request, response);

        /* test */
        PDSServerError PDSServerError = r.getBody();
        assertEquals(TEST_ERROR_MESSAGE, PDSServerError.message);

    }

    @Test
    void using_default_errorattributes_when_response_is_501_the_returns_json_body_contains_NOT_the_error_message() {
        /* prepare */
        prepareError(501, TEST_ERROR_MESSAGE);

        /* execute */
        ResponseEntity<PDSServerError> r = controllerToTest.error(request, response);

        /* test */
        PDSServerError PDSServerError = r.getBody();
        assertEquals(null, PDSServerError.message);

    }

    void prepareError(int httpStatus, String message) {
        when(response.getStatus()).thenReturn(httpStatus);
        when(request.getAttribute(RequestDispatcher.ERROR_MESSAGE)).thenReturn(message);
    }
}
