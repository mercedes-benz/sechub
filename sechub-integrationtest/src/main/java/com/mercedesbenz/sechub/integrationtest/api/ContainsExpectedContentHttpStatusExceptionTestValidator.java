package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.Assert.*;

import org.springframework.web.client.HttpStatusCodeException;

public class ContainsExpectedContentHttpStatusExceptionTestValidator implements HttpStatusCodeExceptionTestValidator {

    private String expectedContent;

    public ContainsExpectedContentHttpStatusExceptionTestValidator(String expectedContent) {
        this.expectedContent = expectedContent;
    }

    @Override
    public void validate(HttpStatusCodeException exception) {
        if (exception == null) {
            fail("no exception!??!?");
        }
        String responseBody = exception.getResponseBodyAsString();
        if (!responseBody.contains(expectedContent)) {
            String message = "The expected content was not contained inside response body.\nException message was:" + exception.getMessage();
            assertEquals(message, expectedContent, responseBody);
        }

    }

}
