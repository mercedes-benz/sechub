// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TargetConnectionCheckerTest {

    private TargetConnectionChecker connectionCheckerToTest;

    @BeforeEach
    void beforeEach() {
        connectionCheckerToTest = new TargetConnectionChecker();
    }

    @ParameterizedTest
    @ValueSource(ints = { 302, 200, 401, 403, 405 })
    void allowed_response_code_return_true(int responseCode) {
        /* test */
        assertTrue(connectionCheckerToTest.isReponseCodeValid(responseCode));
    }

    @ParameterizedTest
    @ValueSource(ints = { 404, 500, 501 })
    void not_allowed_response_code_return_false(int responseCode) {
        /* test */
        assertFalse(connectionCheckerToTest.isReponseCodeValid(responseCode));
    }

}
