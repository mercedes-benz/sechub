// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.helper;

public class ScanDurationHelper {
    private static final double _10_PERCENT = 0.1;

    private static final double _30_PERCENT = 0.3;

    private static final double _40_PERCENT = 0.4;

    private static final double _50_PERCENT = 0.5;

    private static final double _70_PERCENT = 0.7;

    /**
     *
     * Computes the max duration for the Owasp Zap spider. The computed time depends
     * on how many of the other modules are enabled.
     *
     * @param isActiveScanEnabled
     * @param isAjaxSpiderEnabled
     * @param maxScanDurationInMinutes
     * @return max duration of spider
     */
    public long computeSpiderMaxScanDuration(boolean isActiveScanEnabled, boolean isAjaxSpiderEnabled, long maxScanDurationInMinutes) {
        // if active scan and ajax spider are enabled the spider gets 10% of the scan
        // remaining time
        if (isActiveScanEnabled && isAjaxSpiderEnabled) {
            return (long) (maxScanDurationInMinutes * _10_PERCENT);

        }

        // if either active scan or ajax spider are enabled the spider gets 30% of the
        // remaining scan time
        if (!isActiveScanEnabled ^ !isAjaxSpiderEnabled) {
            return (long) (maxScanDurationInMinutes * _30_PERCENT);
        }

        // if active scan and ajax spider are disabled, spider gets half of the
        // remaining scan time
        return (long) (maxScanDurationInMinutes * _50_PERCENT);
    }

    /**
     * Computes the max duration for the Owasp Zap spider. The computed time depends
     * on how many of the other modules are enabled.
     *
     * @param isActiveScanEnabled
     * @param maxScanDurationInMinutes
     * @return max duration of ajax spider
     */
    public long computeAjaxSpiderMaxScanDuration(boolean isActiveScanEnabled, long maxScanDurationInMinutes) {
        if (isActiveScanEnabled) {
            // if activeScan is enabled the ajaxSpider gets 40% of the remaining scan time
            return (long) (maxScanDurationInMinutes * _40_PERCENT);
        }
        // if activeScan is disabled the ajaxSpider gets 70% of the remaining scan time
        return (long) (maxScanDurationInMinutes * _70_PERCENT);
    }

    /**
     * Computes the max duration for the Owasp Zap spider. The computed time depends
     * on how many of the other modules are enabled.
     *
     * @param isActiveScanEnabled
     * @param isAjaxSpiderEnabled
     * @param maxScanDurationInMinutes
     * @return max duration of active spider
     */
    public long computePassiveScanMaxScanDuration(boolean isActiveScanEnabled, boolean isAjaxSpiderEnabled, long maxScanDurationInMinutes) {
        // if active scan and ajax spider are enabled the passive scan 10% of the scan
        // time
        if (isActiveScanEnabled && isAjaxSpiderEnabled) {
            return (long) (maxScanDurationInMinutes * _10_PERCENT);

        }

        // if active scan is enabled but ajax spider is disabled passive scan 30% of the
        // remaining scan time
        if (isActiveScanEnabled && !isAjaxSpiderEnabled) {
            return (long) (maxScanDurationInMinutes * _30_PERCENT);
        }

        // if active scan and ajax spider are disabled, passive scan gets the remaining
        // scan time
        return maxScanDurationInMinutes;
    }

}
