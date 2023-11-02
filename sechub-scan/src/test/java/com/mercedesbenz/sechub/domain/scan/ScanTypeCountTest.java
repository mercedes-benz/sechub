// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.ScanType;

public class ScanTypeCountTest {

    private ScanTypeCount scanTypeCount;

    @BeforeEach
    void beforeEach() {
        scanTypeCount = ScanTypeCount.of(ScanType.CODE_SCAN);
    }

    @Test
    void of_constructor_creates_right_object() {
        /* prepare + execute */
        scanTypeCount = ScanTypeCount.of(ScanType.CODE_SCAN);

        /* test */
        assertEquals(scanTypeCount.getScanType(), ScanType.CODE_SCAN);
        assertEquals(scanTypeCount.getHighSeverityCount(), 0);
        assertEquals(scanTypeCount.getMediumSeverityCount(), 0);
        assertEquals(scanTypeCount.getLowSeverityCount(), 0);
    }

    @Test
    void when_ScanType_is_null_then_of_constructor_throws_IllegalArgumentException() {
        /* prepare */
        String expectedMessage = "ScanType argument must exist";

        /* execute */
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ScanTypeCount.of(null);
        });

        /* test */
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void execute_incrementHighSeverityCount_once_increment_highSeverityCount_value_by_one() {
        /* execute */
        scanTypeCount.incrementHighSeverityCount();

        /* test */
        assertEquals(scanTypeCount.getHighSeverityCount(), 1);
    }

    @Test
    void executing_incrementHighSeverityCount_101_times_increases_highSeverityCount_value_by_101() {
        /* execute */
        for (int i = 0; i < 101; i++) {
            scanTypeCount.incrementHighSeverityCount();
        }

        /* test */
        assertEquals(scanTypeCount.getHighSeverityCount(), 101);
    }

    @Test
    void execute_incrementMediumSeverityCount_once_increment_mediumSeverityCount_value_by_one() {
        /* execute */
        scanTypeCount.incrementMediumSeverityCount();

        /* test */
        assertEquals(scanTypeCount.getMediumSeverityCount(), 1);
    }

    @Test
    void executing_incrementMediumSeverityCount_101_times_increases_mediumSeverityCount_value_by_101() {
        /* execute */
        for (int i = 0; i < 101; i++) {
            scanTypeCount.incrementMediumSeverityCount();
        }

        /* test */
        assertEquals(scanTypeCount.getMediumSeverityCount(), 101);
    }

    @Test
    void execute_incrementLowSeverityCount_once_increment_lowSeverityCount_value_by_one() {
        /* execute */
        scanTypeCount.incrementLowSeverityCount();

        /* test */
        assertEquals(scanTypeCount.getLowSeverityCount(), 1);
    }

    @Test
    void executing_incrementLowSeverityCount_101_times_increases_lowSeverityCount_value_by_101() {
        /* execute */
        for (int i = 93; i < 101; i++) {
            scanTypeCount.incrementLowSeverityCount();
        }

        /* test */
        assertEquals(scanTypeCount.getLowSeverityCount(), 101);
    }

    @Test
    void compareTo_must_return_positive_on_scanTypeCount_is_null() {
        /* prepare */
        ScanTypeCount scanTypeCountA = ScanTypeCount.of(ScanType.CODE_SCAN);
        ScanTypeCount scanTypeCountB = null;

        /* execute */
        int compareResult = scanTypeCountA.compareTo(scanTypeCountB);

        /* test */
        assertTrue(0 < compareResult);
    }

    @Test
    void compareTo_must_return_zero_on_equals_scanTypeCounts() {
        /* prepare */
        ScanTypeCount scanTypeCountA = ScanTypeCount.of(ScanType.CODE_SCAN);
        ScanTypeCount scanTypeCountB = ScanTypeCount.of(ScanType.CODE_SCAN);

        /* execute */
        int compareResult = scanTypeCountA.compareTo(scanTypeCountB);

        /* test */
        assertEquals(0, compareResult);
    }

    @Test
    void compareTo_must_return_positive_value_because_scanTypeCountA_different_from_scanTypeCountB() {
        /* prepare */
        ScanTypeCount scanTypeCountA = ScanTypeCount.of(ScanType.CODE_SCAN);
        ScanTypeCount scanTypeCountB = ScanTypeCount.of(ScanType.WEB_SCAN);

        /* execute */
        int compareResult = scanTypeCountA.compareTo(scanTypeCountB);

        /* test */
        assertTrue(0 < compareResult);
    }

    @Test
    void compareTo_must_return_negative_value_because_scanTypeCountA_different_from_scanTypeCountB() {
        /* prepare */
        ScanTypeCount scanTypeCountA = ScanTypeCount.of(ScanType.WEB_SCAN);
        ScanTypeCount scanTypeCountB = ScanTypeCount.of(ScanType.CODE_SCAN);

        /* execute */
        int compareResult = scanTypeCountA.compareTo(scanTypeCountB);

        /* test */
        assertTrue(0 > compareResult);
    }

    @Test
    void when_highSeverityCount_equals_MAX_VALUE_then_after_increasing_it_must_be_less_than_0() {
        /* prepare */
        scanTypeCount.highSeverityCount = Long.MAX_VALUE;

        /* execute */
        scanTypeCount.incrementHighSeverityCount();

        /* test */
        assertTrue(0 > scanTypeCount.getHighSeverityCount());
    }

    @Test
    void when_mediumSeverityCount_equals_MAX_VALUE_then_after_increasing_it_must_be_less_than_0() {
        /* prepare */
        scanTypeCount.mediumSeverityCount = Long.MAX_VALUE;

        /* execute */
        scanTypeCount.incrementMediumSeverityCount();

        /* test */
        assertTrue(0 > scanTypeCount.getMediumSeverityCount());
    }

    @Test
    void when_lowSeverityCount_equals_MAX_VALUE_then_after_increasing_it_must_be_less_than_0() {
        /* prepare */
        scanTypeCount.lowSeverityCount = Long.MAX_VALUE;

        /* execute */
        scanTypeCount.incrementLowSeverityCount();

        /* test */
        assertTrue(0 > scanTypeCount.getLowSeverityCount());
    }

}
