// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SecHubReportScanTypeSummaryTest {

    private SecHubReportScanTypeSummary summaryToTest;

    @BeforeEach
    void beforeEach() {
        summaryToTest = new SecHubReportScanTypeSummary();
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 10 })
    void increment_critical(int incrementions) {
        /* execute */
        for (int i = 0; i < incrementions; i++) {
            summaryToTest.incrementCritical();
        }

        /* test */
        assertEquals(incrementions, summaryToTest.getCritical());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 10 })
    void increment_high(int incrementions) {
        /* execute */
        for (int i = 0; i < incrementions; i++) {
            summaryToTest.incrementHigh();
        }

        /* test */
        assertEquals(incrementions, summaryToTest.getHigh());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 10 })
    void increment_medium(int incrementions) {
        /* execute */
        for (int i = 0; i < incrementions; i++) {
            summaryToTest.incrementMedium();
        }

        /* test */
        assertEquals(incrementions, summaryToTest.getMedium());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 10 })
    void increment_low(int incrementions) {
        /* execute */
        for (int i = 0; i < incrementions; i++) {
            summaryToTest.incrementLow();
        }

        /* test */
        assertEquals(incrementions, summaryToTest.getLow());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 10 })
    void increment_unclassified(int incrementions) {
        /* execute */
        for (int i = 0; i < incrementions; i++) {
            summaryToTest.incrementUnclassified();
        }

        /* test */
        assertEquals(incrementions, summaryToTest.getUnclassified());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 10 })
    void increment_info(int incrementions) {
        /* execute */
        for (int i = 0; i < incrementions; i++) {
            summaryToTest.incrementInfo();
        }

        /* test */
        assertEquals(incrementions, summaryToTest.getInfo());
    }

}
