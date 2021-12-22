// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import static com.daimler.sechub.adapter.TimeConstants.*;

import java.net.InetAddress;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.crypto.SealedObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.adapter.support.URIShrinkSupport;
import com.daimler.sechub.commons.core.security.CryptoAccess;

public abstract class AbstractAdapterConfigBuilder<B extends AbstractAdapterConfigBuilder<B, C>, C extends AdapterConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractAdapterConfigBuilder.class);

    // scan result check period
    private static final int DEFAULT_SCAN_RESULT_CHECK_IN_MILLISECONDS = TIME_1_MINUTE_IN_MILLISECONDS;
    private static final int MIN_SCAN_RESULT_CHECK_IN_MILLISECONDS = 500;
    private static final int MAX_SCAN_RESULT_CHECK_IN_MILLISECONDS = TIME_1_HOUR_IN_MILLISECONDS;

    private static final int DEFAULT_TIMEOUT_IN_MINUTES = TIME_5_DAYS_IN_MINUTES;
    private static final int MAX_SCAN_TIMEOUT_IN_MINUTES = TIME_5_DAYS_IN_MINUTES;
    private static final int MIN_SCAN_TIMEOUT_IN_MINUTES = 1;

    public static final String DOCUMENT_INFO_TIMEOUT_IN_MINUTES = "Time in minutes when adapter result check will automatically time out and adapter stops execution automatically. When -1 timeout is "
            + AbstractAdapterConfigBuilder.DEFAULT_TIMEOUT_IN_MINUTES + " minutes";

    public static final String DOCUMENT_INFO_CHECK_IN_MILLISECONDS = "Time in milliseconds when adapter check operation is called next. When -1 value is "
            + AbstractAdapterConfigBuilder.DEFAULT_SCAN_RESULT_CHECK_IN_MILLISECONDS + " minutes";

    public static final String DOCUMENT_INFO_CHECK_IN_MINUTES = "Time in minutes when adapter check operation is called next. When -1 value is "
            + (AbstractAdapterConfigBuilder.DEFAULT_SCAN_RESULT_CHECK_IN_MILLISECONDS / 1000 / 60) + " minutes";

    public static final String DOCUMENT_INFO_TRUSTALL = "Turns off certification checks for this product only. Should only be used in test or development environments!";

    private String traceID;
    private String user;
    private String productBaseURL;

    private int timeToWaitForNextCheckOperationInMilliseconds = DEFAULT_SCAN_RESULT_CHECK_IN_MILLISECONDS * 60 * 1000;// one minute check default
    private int timeOutInMinutes = DEFAULT_TIMEOUT_IN_MINUTES;

    private String proxyHostname;

    private int proxyPort;

    private SealedObject passwordOrApiToken;

    private String policyID;

    private boolean trustAllCertificatesEnabled;

    private URIShrinkSupport uriShrinker;

    private String projectId;

    private Map<AdapterOptionKey, String> options = new LinkedHashMap<>();

    private static int minimumTimeToWaitForNextCheckOperationInMilliseconds = 500;

    protected AbstractAdapterConfigBuilder() {
        uriShrinker = createURIShrinker();
    }

    /**
     * Hook for tests
     * 
     * @return new shrinker, never <code>null</code>
     */
    protected URIShrinkSupport createURIShrinker() {
        return new URIShrinkSupport();
    }

    /**
     * Configure this builder by given strategy
     * 
     * @param strategy
     * @return builder (configured by strategy)
     */
    @SuppressWarnings("unchecked")
    public final B configure(AdapterConfigurationStrategy strategy) {
        strategy.configure((B) this);
        return (B) this;
    }

    /**
     * Set result check interval in minutes.
     *
     * @param minutes when <0 the setting will be ignored and default value used!"
     *                see {@link #DOCUMENT_INFO_CHECK_IN_MINUTES}
     * @return builder
     */
    @SuppressWarnings("unchecked")
    public final B setTimeToWaitForNextCheckOperationInMinutes(int minutes) {
        if (minutes < 0) {
            return (B) this;
        }
        this.timeToWaitForNextCheckOperationInMilliseconds = minutes * TIME_1_MINUTE_IN_MILLISECONDS;
        return (B) this;
    }

    /**
     * Set result check interval in milliseconds.
     *
     * @param minutes when <0 the setting will be ignored and default value used!"
     *                see {@link #DOCUMENT_INFO_CHECK_IN_MILLISECONDS}
     * @return builder
     */
    @SuppressWarnings("unchecked")
    public final B setTimeToWaitForNextCheckOperationInMilliseconds(int milliseconds) {
        if (milliseconds < 0) {
            return (B) this;
        }
        this.timeToWaitForNextCheckOperationInMilliseconds = milliseconds;
        return (B) this;
    }

    /**
     * Set timeout in minutes. When a adapter call runs longer than defined time,
     * the adapter will stop and return a failure.
     *
     * @param timeOutInMinutes when <0 the setting will be ignored and default value
     *                         used!" see {@link #DOCUMENT_INFO_TIMEOUT_IN_MINUTES}
     * @return
     */
    @SuppressWarnings("unchecked")
    public final B setTimeOutInMinutes(int timeOutInMinutes) {
        if (timeOutInMinutes < 0) {
            return (B) this;
        }
        this.timeOutInMinutes = timeOutInMinutes;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public final B setUser(String userID) {
        this.user = userID;
        return (B) this;
    }

    /**
     * Set password or API token
     * 
     * @param password a password or an API token
     * @return builder
     */
    @SuppressWarnings("unchecked")
    public final B setPasswordOrAPIToken(String password) {
        this.passwordOrApiToken = encrypt(password);
        return (B) this;
    }

    protected SealedObject encrypt(String password) {
        return CryptoAccess.CRYPTO_STRING.seal(password);
    }

    @SuppressWarnings("unchecked")
    public final B setTraceID(String traceID) {
        this.traceID = traceID;
        return (B) this;
    }

    /**
     * Set the base url for the product - e.g. "https://x.y.z:8123"
     * 
     * @param baseURL
     * @return
     */
    @SuppressWarnings("unchecked")
    public final B setProductBaseUrl(String baseURL) {
        this.productBaseURL = baseURL;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public final B setProxyHostname(String hostname) {
        this.proxyHostname = hostname;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public final B setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public final B setPolicyID(String policyID) {
        this.policyID = policyID;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B setProjectId(String projectId) {
        this.projectId = projectId;
        return (B) this;
    }

    /**
     * Set or remove option
     * 
     * @param option key
     * @param value  string representation of option value
     * 
     */
    @SuppressWarnings("unchecked")
    public B setOption(AdapterOptionKey option, String value) {
        if (option == null) {
            return (B) this;
        }
        if (value == null) {
            options.remove(option);
        } else {
            options.put(option, value);
        }
        return (B) this;
    }

    /**
     * Be aware when using this - this highly insecure and should only be used for
     * development. Enabling this, the used adapter will accept all kind of
     * certificates!
     * 
     * @param trustAllCertificates
     * @return
     */
    @SuppressWarnings("unchecked")
    public B setTrustAllCertificates(boolean trustAllCertificates) {
        this.trustAllCertificatesEnabled = trustAllCertificates;
        return (B) this;
    }

    public final C build() {
        validate();
        ensureTimeSetup();
        ensureTraceID();

        C config = buildInitialConfig();
        if (!(config instanceof AbstractAdapterConfig)) {
            throw new IllegalStateException(getClass().getName() + " does not return a child of AbstractAdapterConfig!");
        }
        Set<URI> shrinkedRootURIs = uriShrinker.shrinkToRootURIs(targetURIs);

        AbstractAdapterConfig abstractAdapterConfig = (AbstractAdapterConfig) config;

        abstractAdapterConfig.productBaseURL = productBaseURL;

        abstractAdapterConfig.timeToWaitForNextCheckOperationInMilliseconds = timeToWaitForNextCheckOperationInMilliseconds;
        ensureMinimumTimeToWait(abstractAdapterConfig);
        abstractAdapterConfig.timeOutInMilliseconds = timeOutInMinutes * 60 * 1000;

        abstractAdapterConfig.proxyHostname = proxyHostname;
        abstractAdapterConfig.proxyPort = proxyPort;
        abstractAdapterConfig.user = user;
        abstractAdapterConfig.trustAllCertificatesEnabled = trustAllCertificatesEnabled;
        abstractAdapterConfig.passwordOrAPIToken = passwordOrApiToken;
        abstractAdapterConfig.policyId = policyID;
        abstractAdapterConfig.targetURIs = targetURIs;
        abstractAdapterConfig.rootTargetUris.addAll(shrinkedRootURIs);
        abstractAdapterConfig.targetIPs = targetIPs;

        abstractAdapterConfig.traceID = traceID;
        abstractAdapterConfig.projectId = projectId;
        abstractAdapterConfig.getOptions().putAll(options);

        packageInternalCustomBuild(config);
        customBuild(config);

        return config;
    }

    private void ensureMinimumTimeToWait(AbstractAdapterConfig c) {
        int timeToWaitForNextCheckConfigured = c.timeToWaitForNextCheckOperationInMilliseconds;
        if (timeToWaitForNextCheckConfigured >= minimumTimeToWaitForNextCheckOperationInMilliseconds) {
            return;
        }
        c.timeToWaitForNextCheckOperationInMilliseconds = minimumTimeToWaitForNextCheckOperationInMilliseconds;

        LOG.info("timeToWaitForNextCheckOperationInMilliseconds was only {} - so do fallback to {} ms", timeToWaitForNextCheckConfigured,
                c.timeToWaitForNextCheckOperationInMilliseconds);
    }

    /**
     * Customization method - is package private, so can only be changed inside main
     * adapter package
     * 
     * @param config
     */
    void packageInternalCustomBuild(C config) {
        /* per default do nothing */
    }

    protected abstract void customBuild(C config);

    protected abstract C buildInitialConfig();

    private void ensureTraceID() {
        if (traceID == null) {
            traceID = "FALLBACK_TRACE_ID#" + System.nanoTime();
        }
    }

    private void ensureTimeSetup() {
        ensureValidTimeForNextCheckOperationInMilliseconds();
        ensureValidTimeForTimeOutInMinutes();
    }

    private void ensureValidTimeForNextCheckOperationInMilliseconds() {
        if (timeToWaitForNextCheckOperationInMilliseconds > MAX_SCAN_RESULT_CHECK_IN_MILLISECONDS) {
            LOG.warn(
                    "{} - Configured check interval:{} milliseconds is bigger than maximum value:{} milliseconds. Automatic reset to default value: {} milliseconds. Please check your configuration!",
                    getClass().getSimpleName(), MAX_SCAN_RESULT_CHECK_IN_MILLISECONDS, DEFAULT_SCAN_RESULT_CHECK_IN_MILLISECONDS);
            timeToWaitForNextCheckOperationInMilliseconds = DEFAULT_SCAN_RESULT_CHECK_IN_MILLISECONDS;
        }
        if (timeToWaitForNextCheckOperationInMilliseconds < MIN_SCAN_RESULT_CHECK_IN_MILLISECONDS) {
            LOG.warn(
                    "{} - Configured check interval:{} milliseconds is lower than minimum value:{} milliseconds. Automatic reset to default value: {} milliseconds. Please check your configuration!",
                    getClass().getSimpleName(), timeToWaitForNextCheckOperationInMilliseconds, MIN_SCAN_RESULT_CHECK_IN_MILLISECONDS,
                    DEFAULT_SCAN_RESULT_CHECK_IN_MILLISECONDS);
            timeToWaitForNextCheckOperationInMilliseconds = DEFAULT_SCAN_RESULT_CHECK_IN_MILLISECONDS;
        }
    }

    private void ensureValidTimeForTimeOutInMinutes() {
        if (timeOutInMinutes > MAX_SCAN_TIMEOUT_IN_MINUTES) {
            LOG.warn(
                    "{} - Configured scan timeout:{} minutes is bigger than maximum value:{} minutes. Automatic reset to maximum done. Please check your configuration!",
                    getClass().getSimpleName(), timeOutInMinutes, MAX_SCAN_TIMEOUT_IN_MINUTES);
            timeOutInMinutes = MAX_SCAN_TIMEOUT_IN_MINUTES;
        }

        if (timeOutInMinutes < MIN_SCAN_TIMEOUT_IN_MINUTES) {
            LOG.warn(
                    "{} -Configured scan timeout:{} minutes is lower than minimum value:{} minutes. Automatic reset to minimum done. Please check your configuration!",
                    getClass().getSimpleName(), timeOutInMinutes, MIN_SCAN_TIMEOUT_IN_MINUTES);
            timeOutInMinutes = MIN_SCAN_TIMEOUT_IN_MINUTES;
        }
    }

    private void validate() {
        assertProxyPortSetWhenProxyHostnameDefined();
        customValidate();
    }

    /**
     * Custom validation. Use assert methods defined in
     * {@link AbstractAdapterConfigBuilder} or use your own for new fields. <br>
     * <br>
     * Already initial checked before are:
     * <ul>
     * <li>when a proxy hostname is set the proxy port must be set too</li>
     * <li>product base url set</li>
     * </ul>
     */
    protected abstract void customValidate();

    protected void assertUserSet() {
        if (user == null) {
            throwIllegalArgument("no user given");
        }
    }

    public void assertProjectIdSet() {
        if (projectId == null) {
            throwIllegalArgument("no projectId given");
        }
    }

    protected void assertPasswordSet() {
        if (passwordOrApiToken == null) {
            throwIllegalArgument("no password given");
        }
    }

    protected void assertPolicyIdSet() {
        if (policyID == null) {
            throwIllegalArgument("no policyID given");
        }
    }

    protected void assertProductBaseURLSet() {
        if (productBaseURL == null || productBaseURL.isEmpty()) {
            throwIllegalArgument("no product base url given");
        }
    }

    protected void assertProxyPortSetWhenProxyHostnameDefined() {
        if (isProxyDefinedButPortMissing()) {
            throwIllegalState("Proxy set, but no port!");
        }
    }

    private boolean isProxyDefinedButPortMissing() {
        return proxyHostname != null && !proxyHostname.isEmpty() && proxyPort == 0;
    }

    private void throwIllegalArgument(String message) throws IllegalArgumentException {
        throw new IllegalArgumentException(message + " in " + getClass().getSimpleName());
    }

    private void throwIllegalState(String message) throws IllegalStateException {
        throw new IllegalStateException(message + " in " + getClass().getSimpleName());
    }

}