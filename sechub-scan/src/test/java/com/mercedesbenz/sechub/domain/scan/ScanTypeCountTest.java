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
        /* prepare + execute */
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ScanTypeCount.of(null);
        });
        String expectedMessage = "ScanType argument must exist";
        String actualMessage = exception.getMessage();

        /* test */
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void incrementHighSeverityCount_do_increment_highSeverityCount_value_by_one() {
        /* execute */
        scanTypeCount.incrementHighSeverityCount();

        /* test */
        assertEquals(scanTypeCount.getHighSeverityCount(), 1);
    }

    @Test
    void incrementMediumSeverityCount_do_increment_mediumSeverityCount_value_by_one() {
        /* execute */
        scanTypeCount.incrementMediumSeverityCount();

        /* test */
        assertEquals(scanTypeCount.getMediumSeverityCount(), 1);
    }

    @Test
    void incrementLowSeverityCount_do_increment_lowSeverityCount_value_by_one() {
        /* execute */
        scanTypeCount.incrementLowSeverityCount();

        /* test */
        assertEquals(scanTypeCount.getLowSeverityCount(), 1);
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
    void compareTo_must_return_positive_value_because_scanTypeCountA_smaller_scanTypeCountB() {
        /* prepare */
        ScanTypeCount scanTypeCountA = ScanTypeCount.of(ScanType.CODE_SCAN);
        ScanTypeCount scanTypeCountB = ScanTypeCount.of(ScanType.WEB_SCAN);

        /* execute */
        int compareResult = scanTypeCountA.compareTo(scanTypeCountB);

        /* test */
        assertTrue(0 < compareResult);
    }

    @Test
    void compareTo_must_return_negative_value_bacause_scanTypeCountA_bigger_scanTypeCountB() {
        /* prepare */
        ScanTypeCount scanTypeCountA = ScanTypeCount.of(ScanType.WEB_SCAN);
        ScanTypeCount scanTypeCountB = ScanTypeCount.of(ScanType.CODE_SCAN);

        /* execute */
        int compareResult = scanTypeCountA.compareTo(scanTypeCountB);

        /* test */
        assertTrue(0 > compareResult);
    }
}
