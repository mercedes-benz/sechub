// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class JSonMessageHttpStatusExceptionTestValidator extends JsonErrorFieldNamesAvailableHttpStatusExceptionTestValidator {

    private String expectedMessage;

    public JSonMessageHttpStatusExceptionTestValidator(HttpStatus status, String expectedMesssage) {
        super(status);
        this.expectedMessage = expectedMesssage;
    }

    @Override
    protected void validateMessageField(Map<String, String> map, HttpStatusCodeException exception) {
        super.validateMessageField(map, exception);
        String message = map.get(FIELD_MESSAGE);
        assertEquals("The message inside JSON is not as expected", expectedMessage, message);
    }
}
