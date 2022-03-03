// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

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

    @Test
    void repsonse_code_less_than_400_accepted() {
        /* test */
        assertTrue(TargetConnectionChecker.isReponseCodeValid(302));
        assertTrue(TargetConnectionChecker.isReponseCodeValid(200));
    }

    @Test
    void edge_case_repsonse_code_400_returns_false() {
        /* test */
        // edge case is not allowed
        assertFalse(TargetConnectionChecker.isReponseCodeValid(400));
    }

    @Test
    void not_allowed_response_code_return_false() {
        /* test */
        assertFalse(TargetConnectionChecker.isReponseCodeValid(404));
        assertFalse(TargetConnectionChecker.isReponseCodeValid(500));
        assertFalse(TargetConnectionChecker.isReponseCodeValid(501));
    }

}
