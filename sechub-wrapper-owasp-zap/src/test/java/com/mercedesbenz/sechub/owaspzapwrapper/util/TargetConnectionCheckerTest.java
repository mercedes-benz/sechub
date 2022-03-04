// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TargetConnectionCheckerTest {

    @Test
    void repsonse_code_401_accepted() {
        /* test */
        // accept 401-unauthorized as target is reachable
        assertTrue(TargetConnectionChecker.isReponseCodeValid(401));
    }

    @Test
    void repsonse_code_403_accepted() {
        /* test */
        // accept 403-forbidden as target is reachable
        assertTrue(TargetConnectionChecker.isReponseCodeValid(403));
    }

    @ParameterizedTest
    @ValueSource(ints = { 302, 200, })
    void repsonse_code_less_than_400_accepted(int responseCode) {
        /* test */
        assertTrue(TargetConnectionChecker.isReponseCodeValid(responseCode));
    }

    @Test
    void edge_case_repsonse_code_400_returns_false() {
        /* test */
        // edge case is not allowed
        assertFalse(TargetConnectionChecker.isReponseCodeValid(400));
    }

    @ParameterizedTest
    @ValueSource(ints = { 404, 500, 501, })
    void not_allowed_response_code_return_false(int responseCode) {
        /* test */
        assertFalse(TargetConnectionChecker.isReponseCodeValid(responseCode));
    }

}
