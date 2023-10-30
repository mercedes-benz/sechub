// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.helper;

import static com.mercedesbenz.sechub.zapwrapper.helper.ScanPercentageConstants.*;

public class ScanDurationHelper {

    /**
     *
     * Computes the max duration for the Zap spider. The computed time depends on
     * how many of the other modules are enabled.
     *
     * @param isActiveScanEnabled
     * @param isAjaxSpiderEnabled
     * @param maxScanDurationInMinutes
     * @return max duration of spider
     */
    public long computeSpiderMaxScanDuration(boolean isActiveScanEnabled, boolean isAjaxSpiderEnabled, long maxScanDurationInMinutes) {
        double percentage = 0;

        if (isActiveScanEnabled && isAjaxSpiderEnabled) {
            percentage = MAX_DURATION_PERCENTAGE_FOR_BOTH_ACTIVE;
        } else if (isActiveScanEnabled || isAjaxSpiderEnabled) {
            percentage = SPIDER_MAX_DURATION_PERCENTAGE_WHEN_ONLY_ONE_ACTIVE;
        } else {
            percentage = MAX_DURATION_PERCENTAGE_WHEN_NONE_ACTIVE;
        }
        return (long) (maxScanDurationInMinutes * percentage);
    }

    /**
     * Computes the max duration for the Zap spider. The computed time depends on
     * how many of the other modules are enabled.
     *
     * @param isActiveScanEnabled
     * @param maxScanDurationInMinutes
     * @return max duration of ajax spider
     */
    public long computeAjaxSpiderMaxScanDuration(boolean isActiveScanEnabled, long maxScanDurationInMinutes) {
        double percentage = 0;

        if (isActiveScanEnabled) {
            percentage = AJAX_SPIDER_MAX_DURATION_PERCENTAGE_WHEN_ACTIVE;
        } else {
            percentage = AJAX_SPIDER_MAX_DURATION_PERCENTAGE_WHEN_NONE_ACTIVE;
        }
        return (long) (maxScanDurationInMinutes * percentage);
    }

    /**
     * Computes the max duration for the Zap spider. The computed time depends on
     * how many of the other modules are enabled.
     *
     * @param isActiveScanEnabled
     * @param isAjaxSpiderEnabled
     * @param maxScanDurationInMinutes
     * @return max duration of active spider
     */
    public long computePassiveScanMaxScanDuration(boolean isActiveScanEnabled, boolean isAjaxSpiderEnabled, long maxScanDurationInMinutes) {
        double percentage = 0;

        if (isActiveScanEnabled && isAjaxSpiderEnabled) {
            percentage = MAX_DURATION_PERCENTAGE_FOR_BOTH_ACTIVE;
        } else if (isActiveScanEnabled && !isAjaxSpiderEnabled) {
            percentage = PASSIVE_SCAN_MAX_DURATION_PERCENTAGE_WHEN_ACTIVE;
        } else {
            return maxScanDurationInMinutes;
        }
        return (long) (maxScanDurationInMinutes * percentage);
    }

}
