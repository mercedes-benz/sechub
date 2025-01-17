// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.integrationtest.api.TestOnlyForRegularExecution;

@TestOnlyForRegularExecution
public class AbstractGrowingSecHubServerTestScenarioTest {

    @Test
    void onlysanitiychecktestscenarioA1_has_main_id_o01() {
        assertEquals("o01", new OnlySanitiyCheckTestScenarioA1().getPrefixMainId());
    }

    @Test
    void xonlysanitiychecktestscenarioB99_has_main_id_o99() {
        assertEquals("x99", new XOnlySanitiyCheckTestScenarioB99().getPrefixMainId());
    }

    @Test
    void onlysanitiychecktestscenarioCnoNumber_will_fail_with_exception() {
        assertThrows(IllegalStateException.class, () -> new OnlySanitiyCheckTestScenarioCnoNumber().getPrefixMainId());
    }

    private class OnlySanitiyCheckTestScenarioA1 extends AbstractGrowingSecHubServerTestScenario {

        @Override
        protected void initializeTestData() {

        }

    }

    private class XOnlySanitiyCheckTestScenarioB99 extends AbstractGrowingSecHubServerTestScenario {

        @Override
        protected void initializeTestData() {

        }

    }

    private class OnlySanitiyCheckTestScenarioCnoNumber extends AbstractGrowingSecHubServerTestScenario {

        @Override
        protected void initializeTestData() {

        }

    }

}
