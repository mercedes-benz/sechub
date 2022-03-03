// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.helper;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertTrue(spiderDuration == 5000);
        assertTrue(passiveScanDuration == 5000);
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
        assertTrue(ajaxSpiderDuration == 7000);
        assertTrue(spiderDuration == 900);
        assertTrue(passiveScanDuration == 2100);
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
        assertTrue(spiderDuration == 3000);
        assertTrue(passiveScanDuration == 2100);
        assertTrue(activeScanDuration == 4900);
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
        assertTrue(ajaxSpiderDuration == 4000);
        assertTrue(spiderDuration == 600);
        assertTrue(passiveScanDuration == 540);
        assertTrue(activeScanDuration == 4860);
    }

}
