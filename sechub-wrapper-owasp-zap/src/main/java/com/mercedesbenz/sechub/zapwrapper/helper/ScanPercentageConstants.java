// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.helper;

public class ScanPercentageConstants {

    /**
     * if active scan and ajax spider are disabled spider and passive scan get this
     * percentage of the remaining scan time
     */
    public static final double MAX_DURATION_PERCENTAGE_WHEN_NONE_ACTIVE = 0.5;

    /**
     * if active scan and ajax spider are enabled the spider and the passive scan
     * get this percentage of the remaining scan time
     */
    public static final double MAX_DURATION_PERCENTAGE_FOR_BOTH_ACTIVE = 0.1;

    /**
     * if either active scan or ajax spider are enabled the spider gets this
     * percentage of the remaining scan time
     */
    public static final double SPIDER_MAX_DURATION_PERCENTAGE_WHEN_ONLY_ONE_ACTIVE = 0.3;

    /**
     * if active scan is enabled the ajax spider gets this percentage of the
     * remaining scan time
     */
    public static final double AJAX_SPIDER_MAX_DURATION_PERCENTAGE_WHEN_ACTIVE = 0.4;

    /**
     * if active scan is disabled the ajax spider gets this percentage of the
     * remaining scan time
     */
    public static final double AJAX_SPIDER_MAX_DURATION_PERCENTAGE_WHEN_NONE_ACTIVE = 0.7;

    /**
     * if the active scan is enabled and the ajax spider is disabled the passive
     * scan get this percentage of the remaining scan time. We need to check for
     * ajax spider as well here, because it was already executed before the passive
     * scan.
     */
    public static final double PASSIVE_SCAN_MAX_DURATION_PERCENTAGE_WHEN_ACTIVE = 0.3;

}
