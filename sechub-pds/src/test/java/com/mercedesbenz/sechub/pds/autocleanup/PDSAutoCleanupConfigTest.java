// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.autocleanup;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.mercedesbenz.sechub.pds.autocleanup.PDSAutoCleanupConfig.CleanupTime;
import com.mercedesbenz.sechub.sharedkernel.CountableInDaysTimeUnit;

class PDSAutoCleanupConfigTest {

    @CsvSource({ "WEEK,3", "MONTH,1", "YEAR,1", "DAY,0" })
    @ParameterizedTest
    void can_be_converted_to_json(String timeUnit, int amount) {
        /* prepare */
        PDSAutoCleanupConfig config = new PDSAutoCleanupConfig();
        CleanupTime cleanupTime = config.getCleanupTime();
        cleanupTime.setAmount(amount);
        cleanupTime.setUnit(CountableInDaysTimeUnit.valueOf(timeUnit));

        /* execute 1 */
        String json = config.toJSON();

        /* test 1 */
        assertNotNull(json);

        /* execute 2 - reverse step */
        PDSAutoCleanupConfig created = PDSAutoCleanupConfig.fromString(json);

        /* test 2 */
        assertNotNull(created);
    }

    @Test
    void default_is_zero_month() {
        /* execute */
        PDSAutoCleanupConfig config = new PDSAutoCleanupConfig();

        /* test */
        CleanupTime cleanupTime = config.getCleanupTime();
        assertEquals(CountableInDaysTimeUnit.MONTH, cleanupTime.getUnit());
        assertEquals(0, cleanupTime.getAmount());
    }
}
