// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx.support;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CheckmarxOAuthDataTest {

    @Test
    void calculateMillisecondsBeforeTokenExpires_is_smaller_0_when_no_expiration_seconds_set() {
        /* prepare */
        CheckmarxOAuthData data = new CheckmarxOAuthData();

        /* execute */
        long expiresInMilliseconds = data.calculateMillisecondsBeforeTokenExpires();

        /* test */
        assertTrue(expiresInMilliseconds <= 0); // negative is also possible
    }

    @Test
    void calculateMillisecondsBeforeTokenExpires_is_less_or_equal_2000_but_always_bigger_than_1800__when_expires_in_2_second_and_checked_immediately() {
        /* prepare */
        CheckmarxOAuthData data = new CheckmarxOAuthData();
        data.expiresInSeconds = 2;

        /* execute */
        long expiresInMilliseconds = data.calculateMillisecondsBeforeTokenExpires();

        /* test */
        assertTrue(expiresInMilliseconds >= 1800, "expiresInMilliseconds was:" + expiresInMilliseconds);
        assertTrue(expiresInMilliseconds <= 2000, "expiresInMilliseconds was:" + expiresInMilliseconds);
    }

}
