// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public abstract class AbstractHttpStatusCodeExceptionTestValidator implements HttpStatusCodeExceptionTestValidator {

    private HttpStatus[] expectedStatusCodes;

    protected HttpStatus[] getExpectedStatusCodes() {
        return expectedStatusCodes;
    }

    public AbstractHttpStatusCodeExceptionTestValidator(HttpStatus... expectedStatusCodes) {
        /* sanity check */
        assertNoHttp20xInside(expectedStatusCodes);
        this.expectedStatusCodes = expectedStatusCodes;
    }

    private void assertNoHttp20xInside(HttpStatus... expectedStatusCodes) {
        for (HttpStatus expectedStatusCode : expectedStatusCodes) {
            if (expectedStatusCode.is2xxSuccessful()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Wrong implemented unit test!\n");
                sb.append("You defined an expected status code inside your test which belongs to 2xxSuccesful familiy:\n");
                sb.append(expectedStatusCode.getReasonPhrase());
                sb.append("\n");
                sb.append("This status is never a failure - so your test is wrong implemented !");
                throw new IllegalArgumentException(sb.toString());
            }
        }
    }

    @Override
    public final void validate(HttpStatusCodeException exception) {
        if (exception == null) {
            throw new IllegalStateException("Testcase corrupt? Exception is null");
        }
        validateHttpStatusCode(exception);
        customValidate(exception);
    }

    protected void validateHttpStatusCode(HttpStatusCodeException exception) {
        int status = exception.getStatusCode().value();
        boolean failedAsExpected = isExpectedStatusCode(status, expectedStatusCodes);
        if (failedAsExpected) {
            return;
        }
        fail("Expected http status codes were:" + Arrays.asList(expectedStatusCodes) + " but was " + status + "\nMessage:" + exception.getMessage()
                + ",\nContent:" + exception.getResponseBodyAsString());
    }

    protected abstract void customValidate(HttpStatusCodeException exception);

    protected boolean isExpectedStatusCode(int status, HttpStatus... expectedStatusCodes) {
        for (HttpStatus expectedStatusCode : expectedStatusCodes) {
            if (expectedStatusCode.value() == status) {
                return true;
            }
        }
        return false;
    }
}
