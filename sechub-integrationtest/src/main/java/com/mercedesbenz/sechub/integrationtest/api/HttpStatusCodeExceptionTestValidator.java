package com.mercedesbenz.sechub.integrationtest.api;

import org.springframework.web.client.HttpStatusCodeException;

public interface HttpStatusCodeExceptionTestValidator {

    /**
     * Validates given exception - if not valid it will fail with a junit
     * {@link AssertionError}n
     *
     * @param exception
     */
    void validate(HttpStatusCodeException exception);

}
