package com.mercedesbenz.sechub.domain.administration.autocleanup;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.mercedesbenz.sechub.domain.administration.autocleanup.AdministrationAutoCleanupConfig.CleanupTime;
import com.mercedesbenz.sechub.sharedkernel.CountableInDaysTimeunit;

class AdministrationAutoCleanupConfigTest {

    @CsvSource({"WEEK,3","MONTH,1","YEAR,1","DAY,0"})
    @ParameterizedTest
    void can_be_converted_to_json(String timeUnit, int amount) {
        /* prepare */
        AdministrationAutoCleanupConfig config = new AdministrationAutoCleanupConfig();
        CleanupTime cleanupTime = config.getCleanupTime();
        cleanupTime.setAmount(amount);
        cleanupTime.setUnit(CountableInDaysTimeunit.valueOf(timeUnit));

        /* execute 1 */
        String json = config.toJSON();

        /* test 1 */
        assertNotNull(json);

        /* execute 2 - reverse step */
        AdministrationAutoCleanupConfig created = AdministrationAutoCleanupConfig.fromString(json);

        /* test 2 */
        assertNotNull(created);
    }

    
    @Test
    void default_is_zero_month() {
        /* execute */
        AdministrationAutoCleanupConfig config = new AdministrationAutoCleanupConfig();
        
        /* test */
        CleanupTime cleanupTime = config.getCleanupTime();
        assertEquals(CountableInDaysTimeunit.MONTH, cleanupTime.getUnit());
        assertEquals(0, cleanupTime.getAmount());
    }
}
