package com.mercedesbenz.sechub.sharedkernel.autocleanup;

public class AutoCleanupConstants {

    public static final int DEFAULT_INITIAL_DELAY_MILLIS = 5 * 1000; // 5 seconds delay
    public static final int DEFAULT_FIXED_DELAY_MILLIS = 24 * 60 * 60 * 1000; // one day

    public static final int TRIGGER_STEP_NUMBER = 1;
    public static final String TRIGGER_STEP_NAME = "Scheduling";
    public static final String TRIGGER_STEP_DESCRIPTION = "Checks for parts to auto clean.";

    public static final String TRIGGER_STEP_MUST_BE_DOCUMENTED = "Auto cleanup is triggered by a cron job operation - default is one day to delay after last execution. "
            + "As initial delay " + DEFAULT_INITIAL_DELAY_MILLIS
            + " milliseconds are defined. It can be configured different, so when you need to startup a cluster "
            + "time shifted, simply change the initial delay values in your wanted way.";

    public static final String TRIGGER_INITIAL_DELAY_STRING = "${sechub.config.trigger.autoclean.initialdelay:" + DEFAULT_INITIAL_DELAY_MILLIS + "}";
    public static final String TRIGGER_FIXED_DELAY_STRING = "${sechub.config.trigger.autoclean.delay:" + DEFAULT_FIXED_DELAY_MILLIS + "}";

    public static final String GENERAL_END_OF_USECASE_DOMAIN_DESCRIPTION = " domain does auto cleanup old data. This is done periodically. The time period is defined by auto cleanup configuration.";
}
