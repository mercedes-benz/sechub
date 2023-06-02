// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FailUntilAmountOfRunsReachedTest {

    @Test
    void when_0_failing_runs_defined_the_first_and_second_run_will_not_throw_exception() throws Exception {
        /* prepare */
        FailUntilAmountOfRunsReached<TestCanaryException, Void> toTest = new FailUntilAmountOfRunsReached<>(0, new TestCanaryException(), null);

        /* execute + test */
        toTest.runOrFail(); // does not throw an exception
        toTest.runOrFail(); // does not throw an exception

    }

    @Test
    void when_1_failing_run_defined_only_the_second_throws_no_exception() throws Exception {
        /* prepare */
        FailUntilAmountOfRunsReached<TestCanaryException, Void> toTest = new FailUntilAmountOfRunsReached<>(1, new TestCanaryException(), null);

        /* execute + test */
        assertThrows(TestCanaryException.class, () -> toTest.runOrFail());
        toTest.runOrFail(); // second attempt does not throw an exception

    }

    @Test
    void when_2_failing_runs_defined_the_second_throws_also_an_exception_but_not_third() throws Exception {
        /* prepare */
        FailUntilAmountOfRunsReached<TestCanaryException, Void> toTest = new FailUntilAmountOfRunsReached<>(2, new TestCanaryException(), null);

        /* execute + test */
        assertThrows(TestCanaryException.class, () -> toTest.runOrFail());
        assertThrows(TestCanaryException.class, () -> toTest.runOrFail());
        toTest.runOrFail(); // second attempt does not throw an exception

    }

}
