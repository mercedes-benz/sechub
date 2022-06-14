// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.autocleanup;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AdministrationAutoCleanupDaysCalculatorTest {

    private AdministrationAutoCleanupDaysCalculator calculatorToTest;

    @BeforeEach
    void beforeEach() {
        calculatorToTest = new AdministrationAutoCleanupDaysCalculator();
    }

    @CsvSource({ "5,weeks,35", "20,days,20", "1,day,1", "2,months,60", "1,month,30", "1,MONTH,30", "0,MONTH,0", "0,DAY,0" })
    @ParameterizedTest
    void days_calculated_as_expected(int amount, String unit, long expectedDays) {
        /* prepare */
        String json = "{ 'cleanupTime' : { 'unit':'" + unit + "', 'amount': " + amount + "}" + "}";
        AdministrationAutoCleanupConfig config = AdministrationAutoCleanupConfig.fromString(json);

        /* execute */
        long calculatedDays = calculatorToTest.calculateCleanupTimeInDays(config);

        /* test */
        assertEquals(expectedDays, calculatedDays);
    }

    @Test
    void calculation_for_null_is_0_days() {
        /* execute */
        long calculatedForNull = calculatorToTest.calculateCleanupTimeInDays(null);

        /* test */
        assertEquals(0, calculatedForNull);
    }
}
