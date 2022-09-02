// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TargetConnectionCheckerTest {

    private TargetConnectionChecker connectionCheckerToTest;

    @BeforeEach
    void beforeEach() {
        connectionCheckerToTest = new TargetConnectionChecker();
    }

    @Test
    void repsonse_code_401_accepted() {
        /* test */
        // accept 401-unauthorized as target is reachable
        assertTrue(connectionCheckerToTest.isReponseCodeValid(401));
    }

    @Test
    void repsonse_code_403_accepted() {
        /* test */
        // accept 403-forbidden as target is reachable
        assertTrue(connectionCheckerToTest.isReponseCodeValid(403));
    }

    @ParameterizedTest
    @ValueSource(ints = { 302, 200, })
    void repsonse_code_less_than_400_accepted(int responseCode) {
        /* test */
        assertTrue(connectionCheckerToTest.isReponseCodeValid(responseCode));
    }

    @Test
    void edge_case_repsonse_code_400_returns_false() {
        /* test */
        // edge case is not allowed
        assertFalse(connectionCheckerToTest.isReponseCodeValid(400));
    }

    @ParameterizedTest
    @ValueSource(ints = { 404, 500, 501, })
    void not_allowed_response_code_return_false(int responseCode) {
        /* test */
        assertFalse(connectionCheckerToTest.isReponseCodeValid(responseCode));
    }

}
