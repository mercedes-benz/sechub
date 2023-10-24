// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import static com.mercedesbenz.sechub.adapter.TimeConstants.*;

public interface AdapterConfigBuilder {

    public static final int DEFAULT_SCAN_RESULT_CHECK_IN_MILLISECONDS = TIME_1_MINUTE_IN_MILLISECONDS;
    public static final int MIN_SCAN_RESULT_CHECK_IN_MILLISECONDS = 500;
    public static final int MAX_SCAN_RESULT_CHECK_IN_MILLISECONDS = TIME_1_HOUR_IN_MILLISECONDS;

    public static final int DEFAULT_TIMEOUT_IN_MINUTES = TIME_5_DAYS_IN_MINUTES;
    public static final int MAX_SCAN_TIMEOUT_IN_MINUTES = TIME_5_DAYS_IN_MINUTES;
    public static final int MIN_SCAN_TIMEOUT_IN_MINUTES = 1;

    public static final String DOCUMENT_INFO_TIMEOUT_IN_MINUTES = "Time in minutes when adapter result check will automatically time out and adapter stops execution automatically. When -1 timeout is "
            + AbstractAdapterConfigBuilder.DEFAULT_TIMEOUT_IN_MINUTES + " minutes";

    public static final String DOCUMENT_INFO_CHECK_IN_MILLISECONDS = "Time in milliseconds when adapter check operation is called next. When -1 value is "
            + AbstractAdapterConfigBuilder.DEFAULT_SCAN_RESULT_CHECK_IN_MILLISECONDS + " minutes";

    public static final String DOCUMENT_INFO_CHECK_IN_MINUTES = "Time in minutes when adapter check operation is called next. When -1 value is "
            + (AbstractAdapterConfigBuilder.DEFAULT_SCAN_RESULT_CHECK_IN_MILLISECONDS / 1000 / 60) + " minutes";

    public static final String DOCUMENT_INFO_TRUSTALL = "Turns off certification checks for this product only. Should only be used in test or development environments!";

    /**
     * Configure this builder by given strategy
     *
     * @param strategy
     * @return builder (configured by strategy)
     */
    AdapterConfigBuilder configure(AdapterConfigurationStrategy strategy);

    /**
     * Set result check interval in minutes.
     *
     * @param minutes when <0 the setting will be ignored and default value used!"
     *                see {@link #DOCUMENT_INFO_CHECK_IN_MINUTES}
     * @return builder
     */
    AdapterConfigBuilder setTimeToWaitForNextCheckOperationInMinutes(int minutes);

    /**
     * Set result check interval in milliseconds.
     *
     * @param minutes when <0 the setting will be ignored and default value used!"
     *                see {@link #DOCUMENT_INFO_CHECK_IN_MILLISECONDS}
     * @return builder
     */
    AdapterConfigBuilder setTimeToWaitForNextCheckOperationInMilliseconds(int milliseconds);

    /**
     * Set timeout in minutes. When a adapter call runs longer than defined time,
     * the adapter will stop and return a failure.
     *
     * @param timeOutInMinutes when <0 the setting will be ignored and default value
     *                         used!" see {@link #DOCUMENT_INFO_TIMEOUT_IN_MINUTES}
     * @return
     */
    AdapterConfigBuilder setTimeOutInMinutes(int timeOutInMinutes);

    AdapterConfigBuilder setUser(String userID);

    /**
     * Set password or API token
     *
     * @param password a password or an API token
     * @return builder
     */
    AdapterConfigBuilder setPasswordOrAPIToken(String password);

    AdapterConfigBuilder setTraceID(String traceID);

    /**
     * Set the base url for the product - e.g. "https://x.y.z:8123"
     *
     * @param baseURL
     * @return
     */
    AdapterConfigBuilder setProductBaseUrl(String baseURL);

    AdapterConfigBuilder setProxyHostname(String hostname);

    AdapterConfigBuilder setProxyPort(int proxyPort);

    AdapterConfigBuilder setPolicyID(String policyID);

    AdapterConfigBuilder setProjectId(String projectId);

    /**
     * Set or remove option
     *
     * @param option key
     * @param value  string representation of option value
     *
     */
    AdapterConfigBuilder setOption(AdapterOptionKey option, String value);

    /**
     * Be aware when using this - this highly insecure and should only be used for
     * development. Enabling this, the used adapter will accept all kind of
     * certificates!
     *
     * @param trustAllCertificates
     * @return
     */
    AdapterConfigBuilder setTrustAllCertificates(boolean trustAllCertificates);

}