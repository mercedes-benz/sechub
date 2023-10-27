// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.autocleanup;

import com.mercedesbenz.sechub.sharedkernel.CountableInDaysTimeUnit;

public class PDSAutoCleanupConstants {

    public static final int DEFAULT_INITIAL_DELAY_MILLIS = 5 * 1000; // 5 seconds delay
    public static final int DEFAULT_FIXED_DELAY_MILLIS = 24 * 60 * 60 * 1000; // one day

    public static final int TRIGGER_STEP_NUMBER = 1;
    public static final String TRIGGER_STEP_NAME = "Scheduling";
    public static final String TRIGGER_STEP_DESCRIPTION = "Checks for parts to auto clean.";

    public static final String TRIGGER_STEP_MUST_BE_DOCUMENTED = "Auto cleanup is triggered by a cron job operation - default is one day to delay after last execution. "
            + "As initial delay " + DEFAULT_INITIAL_DELAY_MILLIS
            + " milliseconds are defined. It can be configured differently. This is useful when you need to startup a cluster. Simply change the initial delay values in to allow the cluster to startup.";

    public static final String TRIGGER_INITIAL_DELAY_STRING = "${pds.config.trigger.autoclean.initialdelay:" + DEFAULT_INITIAL_DELAY_MILLIS + "}";
    public static final String TRIGGER_FIXED_DELAY_STRING = "${pds.config.trigger.autoclean.delay:" + DEFAULT_FIXED_DELAY_MILLIS + "}";

    public static final String GENERAL_END_OF_USECASE_DOMAIN_DESCRIPTION = " does auto cleanup old data. This is done periodically. The time period is defined by auto cleanup configuration.";

    public static final CountableInDaysTimeUnit DEFAULT_AUTO_CLEANUP_CONFIG_UNIT = CountableInDaysTimeUnit.DAY;

    public static final int DEFAULT_AUTO_CLEANUP_CONFIG_AMOUNT = 2;

}
