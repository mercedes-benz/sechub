// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.helper;

public class ScanDurationHelper {

    /**
     *
     * Computes the max duration for the owasp zap spider. The computed time depends
     * on how many of the other modules are enabled.
     *
     * @param isActiveScanEnabled
     * @param isAjaxSpiderEnabled
     * @param maxScanDurationInMinutes
     * @return max duration of spider
     */
    public long computeSpiderMaxScanDuration(boolean isActiveScanEnabled, boolean isAjaxSpiderEnabled, long maxScanDurationInMinutes) {
        // if active scan and ajax spider are active the spider gets less time
        // during the scan
        if (isActiveScanEnabled && isAjaxSpiderEnabled) {
            return (long) (maxScanDurationInMinutes * 0.1);

        }

        // if either active scan or ajax spider are active the spider gets less time
        // during the scan but more compared to the case every scan mode is active
        if (!isActiveScanEnabled ^ !isAjaxSpiderEnabled) {
            return (long) (maxScanDurationInMinutes * 0.3);
        }

        // if active scan and ajax spider are disabled, spider gets half of the scan
        // time
        return (long) (maxScanDurationInMinutes * 0.5);
    }

    /**
     * Computes the max duration for the owasp zap spider. The computed time depends
     * on how many of the other modules are enabled.
     *
     * @param isActiveScanEnabled
     * @param maxScanDurationInMinutes
     * @return max duration of ajax spider
     */
    public long computeAjaxSpiderMaxScanDuration(boolean isActiveScanEnabled, long maxScanDurationInMinutes) {
        if (isActiveScanEnabled) {
            return (long) (maxScanDurationInMinutes * 0.4);
        }
        return (long) (maxScanDurationInMinutes * 0.7);
    }

    /**
     * Computes the max duration for the owasp zap spider. The computed time depends
     * on how many of the other modules are enabled.
     *
     * @param isActiveScanEnabled
     * @param isAjaxSpiderEnabled
     * @param maxScanDurationInMinutes
     * @return max duration of active spider
     */
    public long computePassiveScanMaxScanDuration(boolean isActiveScanEnabled, boolean isAjaxSpiderEnabled, long maxScanDurationInMinutes) {
        // if active scan and ajax spider are active the passive scan gets less time
        // during the scan
        if (isActiveScanEnabled && isAjaxSpiderEnabled) {
            return (long) (maxScanDurationInMinutes * 0.1);

        }

        // if active scan is enabled but ajax spider is disabled passive scan gets less
        // time during the scan but more compared to the case every scan mode is active
        if (isActiveScanEnabled && !isAjaxSpiderEnabled) {
            return (long) (maxScanDurationInMinutes * 0.3);
        }

        // if active scan and ajax spider are disabled, passive scan gets the remaining
        // scan time
        return maxScanDurationInMinutes;
    }

}
