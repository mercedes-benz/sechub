// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScanDurationHelperTest {

    private ScanDurationHelper scanDurationHelperToTest;

    @BeforeEach
    void beforeEach() {
        scanDurationHelperToTest = new ScanDurationHelper();
    }

    @Test
    void compute_max_durations_activeScan_disabled_ajaxSpider_disabled() {
        /* prepare */
        boolean activeScanEnabled = false;
        boolean ajaxSpiderEnabled = false;
        long maxScanDuration = 10000;

        /* execute */
        long spiderDuration = scanDurationHelperToTest.computeSpiderMaxScanDuration(activeScanEnabled, ajaxSpiderEnabled, maxScanDuration);
        long remainingMaxDuration = maxScanDuration - spiderDuration;
        long passiveScanDuration = scanDurationHelperToTest.computePassiveScanMaxScanDuration(activeScanEnabled, ajaxSpiderEnabled, remainingMaxDuration);

        /* test */
        assertEquals(5000, spiderDuration);
        assertEquals(5000, passiveScanDuration);
    }

    @Test
    void compute_max_durations_activeScan_disabled_ajaxSpider_enabled() {
        /* prepare */
        boolean activeScanEnabled = false;
        boolean ajaxSpiderEnabled = true;
        long maxScanDuration = 10000;

        /* execute */
        long ajaxSpiderDuration = scanDurationHelperToTest.computeAjaxSpiderMaxScanDuration(activeScanEnabled, maxScanDuration);
        long remainingMaxDuration = maxScanDuration - ajaxSpiderDuration;
        long spiderDuration = scanDurationHelperToTest.computeSpiderMaxScanDuration(activeScanEnabled, ajaxSpiderEnabled, remainingMaxDuration);
        remainingMaxDuration = remainingMaxDuration - spiderDuration;
        long passiveScanDuration = scanDurationHelperToTest.computePassiveScanMaxScanDuration(activeScanEnabled, ajaxSpiderEnabled, remainingMaxDuration);

        /* test */
        assertEquals(7000, ajaxSpiderDuration);
        assertEquals(900, spiderDuration);
        assertEquals(2100, passiveScanDuration);
    }

    @Test
    void compute_max_durations_activeScan_enabled_ajaxSpider_disabled() {
        /* prepare */
        boolean activeScanEnabled = true;
        boolean ajaxSpiderEnabled = false;
        long maxScanDuration = 10000;

        /* execute */
        long spiderDuration = scanDurationHelperToTest.computeSpiderMaxScanDuration(activeScanEnabled, ajaxSpiderEnabled, maxScanDuration);
        long remainingMaxDuration = maxScanDuration - spiderDuration;
        long passiveScanDuration = scanDurationHelperToTest.computePassiveScanMaxScanDuration(activeScanEnabled, ajaxSpiderEnabled, remainingMaxDuration);
        long activeScanDuration = remainingMaxDuration - passiveScanDuration;

        /* test */
        assertEquals(3000, spiderDuration);
        assertEquals(2100, passiveScanDuration);
        assertEquals(4900, activeScanDuration);
    }

    @Test
    void compute_max_durations_activeScan_enabled_ajaxSpider_enabled() {
        /* prepare */
        boolean activeScanEnabled = true;
        boolean ajaxSpiderEnabled = true;
        long maxScanDuration = 10000;

        /* execute */
        long ajaxSpiderDuration = scanDurationHelperToTest.computeAjaxSpiderMaxScanDuration(activeScanEnabled, maxScanDuration);
        long remainingMaxDuration = maxScanDuration - ajaxSpiderDuration;
        long spiderDuration = scanDurationHelperToTest.computeSpiderMaxScanDuration(activeScanEnabled, ajaxSpiderEnabled, remainingMaxDuration);
        remainingMaxDuration = remainingMaxDuration - spiderDuration;
        long passiveScanDuration = scanDurationHelperToTest.computePassiveScanMaxScanDuration(activeScanEnabled, ajaxSpiderEnabled, remainingMaxDuration);
        long activeScanDuration = remainingMaxDuration - passiveScanDuration;

        /* test */
        assertEquals(4000, ajaxSpiderDuration);
        assertEquals(600, spiderDuration);
        assertEquals(540, passiveScanDuration);
        assertEquals(4860, activeScanDuration);
    }

}
