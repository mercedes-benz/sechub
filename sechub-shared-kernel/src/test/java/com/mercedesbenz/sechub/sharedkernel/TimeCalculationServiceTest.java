// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;

class TimeCalculationServiceTest {

    private TimeCalculationService serviceToTest;
    private SystemTimeProvider systemTime;
    private LocalDateTime date_2021_02_01_time_08_15;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new TimeCalculationService();

        systemTime = mock(SystemTimeProvider.class);
        date_2021_02_01_time_08_15 = LocalDateTime.of(2021, 2, 1, 8, 15);

        serviceToTest.systemTime = systemTime;
    }

    @NullSource
    @CsvSource({ "0", "1", "90", "365" })
    @ParameterizedTest
    void now_minus_days_calulation_works(Long days) {
        /* prepare */
        LocalDateTime now = date_2021_02_01_time_08_15;
        when(systemTime.getNow()).thenReturn(now);

        /* execute */
        LocalDateTime calculated = serviceToTest.calculateNowMinusDays(days);

        /* test */
        if (days == null) {
            assertEquals(now.minusDays(0), calculated);
        } else {
            assertEquals(now.minusDays(days), calculated);
        }
    }

    @CsvSource({ "-1", "-100", "-365" })
    @ParameterizedTest
    void now_minus_days_calulation_works_when_negative_days_are_added(long negativeDays) {
        /* prepare */
        LocalDateTime now = date_2021_02_01_time_08_15;
        when(systemTime.getNow()).thenReturn(now);
        long plusDays = Math.abs(negativeDays);

        /* execute */
        LocalDateTime calculated = serviceToTest.calculateNowMinusDays(negativeDays);

        /* test */
        assertEquals(now.plusDays(plusDays), calculated);
    }

}
