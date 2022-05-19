package com.mercedesbenz.sechub.adapter;

public interface AdapterConfigBuilder/* <B extends AdapterConfigBuilder<B, C>, C extends AdapterConfig> */ {

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