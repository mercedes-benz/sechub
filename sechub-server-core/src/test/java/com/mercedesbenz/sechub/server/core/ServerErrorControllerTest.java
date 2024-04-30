// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class ServerErrorControllerTest {

    private static final String TEST_ERROR_MESSAGE = "my SecHub error message";

    private HttpServletResponse response;
    private HttpServletRequest request;
    private ServerErrorController controllerToTest;
    private DefaultErrorAttributes errorAttributes;

    @BeforeEach
    void beforeEach() {
        response = mock(HttpServletResponse.class);
        request = mock(HttpServletRequest.class);
        errorAttributes = new DefaultErrorAttributes();

        controllerToTest = new ServerErrorController();
        controllerToTest.errorAttributes = errorAttributes;
    }

    @Test
    void when_response_is_401_the_json_body_contains_the_error_message() {
        /* prepare */
        prepareError(401, TEST_ERROR_MESSAGE);

        /* execute */
        ResponseEntity<ServerError> r = controllerToTest.error(request, response);

        /* test */
        ServerError serverError = r.getBody();
        assertEquals(TEST_ERROR_MESSAGE, serverError.message);

    }

    @Test
    void when_response_is_401_the_json_body_contains_the_error_message_and_binding_details1() {
        /* prepare */
        FieldError objectError = new FieldError("errorObjectName", "fieldName", "defaultMessageToResolveThis");

        simulateBindingExceptionOnSprintBootServerSide("bindingResultObjectName", objectError);

        prepareError(401, "something");

        /* execute */
        ResponseEntity<ServerError> serverErrorResponse = controllerToTest.error(request, response);

        /* test */
        ServerError serverError = serverErrorResponse.getBody();
        assertEquals("Validation failed for object='bindingResultObjectName'. Error count: 1", serverError.message);

        assertEquals(1, serverError.details.size());
        assertEquals("Field 'fieldName' with value 'null' was rejected. defaultMessageToResolveThis", serverError.details.iterator().next());

    }

    @Test
    void when_response_is_401_the_json_body_contains_the_error_message_and_binding_details2_check_no_stacktrace() {
        /* prepare */
        FieldError objectError1 = new FieldError("errorObjectName1", "fieldName1", "defaultMessageToResolveThis1");
        FieldError objectError2 = new FieldError("errorObjectName2", "fieldName2", "defaultMessageToResolveThis2");

        simulateBindingExceptionOnSprintBootServerSide("bindingResultObjectName", objectError1, objectError2);

        prepareError(401, "something");

        /* execute */
        ResponseEntity<ServerError> serverErrorResponse = controllerToTest.error(request, response);

        /* test */
        ServerError serverError = serverErrorResponse.getBody();
        assertEquals("Validation failed for object='bindingResultObjectName'. Error count: 2", serverError.message);

        assertEquals(2, serverError.details.size());
        Iterator<String> iterator = serverError.details.iterator();
        assertEquals("Field 'fieldName1' with value 'null' was rejected. defaultMessageToResolveThis1", iterator.next());
        assertEquals("Field 'fieldName2' with value 'null' was rejected. defaultMessageToResolveThis2", iterator.next());

        assertNull(serverError.trace);
    }

    @Test
    void when_response_is_401_the_json_body_contains_the_error_message_and_binding_details2_check_with_stacktrace_when_debug() {
        /* prepare */
        FieldError objectError1 = new FieldError("errorObjectName1", "fieldName1", "defaultMessageToResolveThis1");
        FieldError objectError2 = new FieldError("errorObjectName2", "fieldName2", "defaultMessageToResolveThis2");

        simulateBindingExceptionOnSprintBootServerSide("bindingResultObjectName", objectError1, objectError2);

        prepareError(401, "something");
        controllerToTest.debug = true;

        /* execute */
        ResponseEntity<ServerError> serverErrorResponse = controllerToTest.error(request, response);

        /* test */
        ServerError serverError = serverErrorResponse.getBody();
        assertEquals("Validation failed for object='bindingResultObjectName'. Error count: 2", serverError.message);

        assertEquals(2, serverError.details.size());
        Iterator<String> iterator = serverError.details.iterator();
        assertEquals("Field 'fieldName1' with value 'null' was rejected. defaultMessageToResolveThis1", iterator.next());
        assertEquals("Field 'fieldName2' with value 'null' was rejected. defaultMessageToResolveThis2", iterator.next());

        assertNotNull(serverError.trace);
    }

    private void simulateBindingExceptionOnSprintBootServerSide(String objectName, FieldError... objectErrors) {
        BindingResult bindingResult = mock(BindingResult.class);

        List<ObjectError> allErrors = new ArrayList<>();

        allErrors.addAll(Arrays.asList(objectErrors));

        when(bindingResult.getObjectName()).thenReturn(objectName);
        when(bindingResult.getAllErrors()).thenReturn(allErrors);
        when(bindingResult.getErrorCount()).thenReturn(allErrors.size());

        BindException bindException = new BindException(bindingResult);
        when(request.getAttribute(RequestDispatcher.ERROR_EXCEPTION)).thenReturn(bindException);
    }

    @Test
    void when_response_is_501_the_json_body_contains_NOT_the_error_message() {
        /* prepare */
        prepareError(501, TEST_ERROR_MESSAGE);

        /* execute */
        ResponseEntity<ServerError> r = controllerToTest.error(request, response);

        /* test */
        ServerError serverError = r.getBody();
        assertEquals(null, serverError.message);

    }

    void prepareError(int httpStatus, String message) {
        when(response.getStatus()).thenReturn(httpStatus);
        when(request.getAttribute(RequestDispatcher.ERROR_MESSAGE)).thenReturn(message);
    }

}
