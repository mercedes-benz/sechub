// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds;

public class PDSDefaultParameterValueConstants {

    public static final int NO_TIME_TO_WAIT_IN_SECONDS_FOR_SCRIPT_CANCELLATION = 0;
    public static final int DEFAULT_TIME_TO_WAIT_IN_SECONDS_FOR_SCRIPT_CANCELLATION = NO_TIME_TO_WAIT_IN_SECONDS_FOR_SCRIPT_CANCELLATION;
    public static final int MAXIMUM_TIME_TO_WAIT_IN_SECONDS_FOR_SCRIPT_CANCELLATION = 60 * 10; // ten minutes max

    public static final int MINIMUM_TIME_TO_WAIT_IN_MILLISECONDS_FOR_SCRIPT_CANCELLATION_CHECK = 300;
    public static final int DEFAULT_TIME_TO_WAIT_IN_MILLISECONDS_FOR_SCRIPT_CANCELLATION_CHECK = MINIMUM_TIME_TO_WAIT_IN_MILLISECONDS_FOR_SCRIPT_CANCELLATION_CHECK;
    public static final int MAXIMUM_TIME_TO_WAIT_IN_MILLISECONDS_FOR_SCRIPT_CANCELLATION_CHECK = 5000; // 5 seconds

    public static final int DEFAULT_MINUTES_TO_WAIT_FOR_PRODUCRESULT = 60 * 2; // 2 hours
    public static final int MAXIMUM_CONFIGURABLE_TIME_TO_WAIT_FOR_PRODUCT_IN_MINUTES = 60 * 24 * 3; // 3 days...
    public static final int MINIMUM_CONFIGURABLE_TIME_TO_WAIT_FOR_PRODUCT_IN_MINUTES = 1; // 1 minute...

}
