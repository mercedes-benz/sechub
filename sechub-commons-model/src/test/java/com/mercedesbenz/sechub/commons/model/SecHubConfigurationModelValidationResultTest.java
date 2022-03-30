// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidationError.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidationResult.SecHubConfigurationModelValidationErrorData;

class SecHubConfigurationModelValidationResultTest {

    private SecHubConfigurationModelValidationResult resultToTest;

    @BeforeEach
    void beforeEach() {
        resultToTest = new SecHubConfigurationModelValidationResult();
    }

    @Test
    void error_added_has_error_returns_true() {
        /* execute */
        resultToTest.addError(API_VERSION_NOT_SUPPORTED, "additional info");

        /* test */
        assertTrue(resultToTest.hasError(API_VERSION_NOT_SUPPORTED));
    }

    @Test
    void error_added_can_be_fetched() {
        /* execute */
        resultToTest.addError(API_VERSION_NOT_SUPPORTED, "additional info");

        /* test */
        SecHubConfigurationModelValidationErrorData data = resultToTest.findFirstOccurrenceOf(API_VERSION_NOT_SUPPORTED);
        assertNotNull(data);
        assertTrue(data.getMessage().endsWith("additional info"));
        assertTrue(data.getMessage().startsWith(API_VERSION_NOT_SUPPORTED.getDefaultMessage()));
    }

}
