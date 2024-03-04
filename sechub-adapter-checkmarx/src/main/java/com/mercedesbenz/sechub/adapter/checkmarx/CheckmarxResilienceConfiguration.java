package com.mercedesbenz.sechub.adapter.checkmarx;

public interface CheckmarxResilienceConfiguration {

    /*
     * Constants shall be in synch with configuration defaults in
     * "/sechub-pds-solutions/checkmarx/docker/pds-config.json".
     *
     * The PDS defaults are the origin parts - if there are differences update this
     * java file. The constants here are only for direct SecHub communication with
     * CHECKMARX used (which is no longer suggested and more for testing purposes
     * only)
     *
     */
    public static final int DEFAULT_BADREQUEST_RETRY_MAX = 3;
    public static final int DEFAULT_BADREQUEST_RETRY_TIME_TO_WAIT_MILLISECONDS = 2000;

    public static final int DEFAULT_SERVERERROR_RETRY_MAX = 1;
    public static final int DEFAULT_SERVERERROR_RETRY_TIME_TO_WAIT_MILLISECONDS = 5000;

    public static final int DEFAULT_NETWORKERROR_RETRY_MAX = 100;
    public static final int DEFAULT_NETWORKERROR_RETRY_TIME_TO_WAIT_MILLISECONDS = 5000;

    public int getBadRequestMaxRetries();

    public int getBadRequestRetryTimeToWaitInMilliseconds();

    public int getInternalServerErrortMaxRetries();

    public int getInternalServerErrorRetryTimeToWaitInMilliseconds();

    public int getNetworkErrorMaxRetries();

    public int getNetworkErrorRetryTimeToWaitInMilliseconds();
}