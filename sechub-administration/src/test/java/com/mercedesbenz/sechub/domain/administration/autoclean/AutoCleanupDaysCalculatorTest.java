package com.mercedesbenz.sechub.domain.administration.autoclean;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.mercedesbenz.sechub.domain.administration.autocleanup.AutoCleanupConfig;
import com.mercedesbenz.sechub.domain.administration.autocleanup.AutoCleanupDaysCalculator;

class AutoCleanupDaysCalculatorTest {

    private AutoCleanupDaysCalculator calculatorToTest;

    @BeforeEach
    void beforeEach() {
        calculatorToTest = new AutoCleanupDaysCalculator();
    }

    @CsvSource({ "5,weeks,35", "20,days,20", "1,day,1", "2,months,60", "1,month,30", "1,MONTH,30" })
    @ParameterizedTest
    void test(int amount, String unit, long expectedDays) {
        /* prepare */
        String json = "{ 'cleanupTime' : { 'unit':'" + unit + "', 'amount': " + amount + "}" + "}";
        AutoCleanupConfig config = AutoCleanupConfig.fromString(json);

        /* execute */
        long calculatedDays = calculatorToTest.calculateCleanupTimeInDays(config);

        /* test */
        assertEquals(expectedDays, calculatedDays);
    }

    @Test
    void default_is_90_days() {
        /* execute */
        long calculatedWithNewInstance = calculatorToTest.calculateCleanupTimeInDays(new AutoCleanupConfig());

        /* test */
        assertEquals(90, calculatedWithNewInstance);
    }

    @Test
    void null_is_same_as_default_days() {
        /* prepare */
        long calculatedWithNewInstance = calculatorToTest.calculateCleanupTimeInDays(new AutoCleanupConfig());
        /* execute */
        long calculatedForNull = calculatorToTest.calculateCleanupTimeInDays(null);

        /* test */
        assertEquals(calculatedForNull, calculatedWithNewInstance);
    }
}
