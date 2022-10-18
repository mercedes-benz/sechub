// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ProcessHandlingDataTest {

    @Test
    void isStillWaitingAccepted__after_init_0_0_false() {
        /* preapre */
        ProcessHandlingData data = new ProcessHandlingData(0, 0);

        /* execute + test */
        assertFalse(data.isStillWaitingForProcessAccepted());
    }

    @ParameterizedTest
    /* @formatter:off */
    @CsvSource({
        "1,1",
        "900,1",
        "1900,2",
        "2900,3"
    })
/* @formatter:on */
    void isStillWaitingAccepted__when_process_wait_time_not_exceeded__wait_true(int runtimeInMillis, int secondsToWait) {
        /* preapre */
        ProcessHandlingData data = new ProcessHandlingData(secondsToWait, 0);
        data.processStartTimeStamp = System.currentTimeMillis() - runtimeInMillis;

        /* execute + test */
        assertTrue(data.isStillWaitingForProcessAccepted());
    }

    @ParameterizedTest
    /* @formatter:off */
    @CsvSource({
        "1001,1",
        "2001,2",
        "3001,3"
    })
    /* @formatter:on */
    void isStillWaitingAccepted__when_process_wait_time_exceeded__wait_false(int runtimeInMillis, int secondsToWait) {
        /* preapre */
        ProcessHandlingData data = new ProcessHandlingData(secondsToWait, 0);
        data.processStartTimeStamp = System.currentTimeMillis() - runtimeInMillis;

        /* execute + test */
        assertFalse(data.isStillWaitingForProcessAccepted());
    }

}
