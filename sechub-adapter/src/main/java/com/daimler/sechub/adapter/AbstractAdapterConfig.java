// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SealedObject;

import com.daimler.sechub.commons.core.security.CryptoAccess;

public abstract class AbstractAdapterConfig implements AdapterConfig {

    String productBaseURL;

    private SealedObject passwordOrAPITokenBase64encoded;

    int timeToWaitForNextCheckOperationInMilliseconds;

    int timeOutInMilliseconds;

    int proxyPort;

    String proxyHostname;

    String user;

    SealedObject passwordOrAPIToken;

    String policyId;

    String projectId;

    String traceID;

    boolean trustAllCertificatesEnabled;

    private Map<AdapterOptionKey, String> options = new HashMap<>();

    protected AbstractAdapterConfig() {
    }

    @Override
    public final int getTimeOutInMilliseconds() {
        return timeOutInMilliseconds;
    }

    @Override
    public int getTimeToWaitForNextCheckOperationInMilliseconds() {
        return timeToWaitForNextCheckOperationInMilliseconds;
    }

    @Override
    public boolean isTrustAllCertificatesEnabled() {
        return trustAllCertificatesEnabled;
    }

    @Override
    public final String getProductBaseURL() {
        return productBaseURL;
    }

    @Override
    public final String getTraceID() {
        return traceID;
    }

    @Override
    public final String getCredentialsBase64Encoded() {
        if (passwordOrAPIToken == null) {
            return null;
        }
        if (passwordOrAPITokenBase64encoded == null) {
            String tokenString = user + ":" + CryptoAccess.CRYPTO_STRING.unseal(passwordOrAPIToken);
            byte[] tokenBytes = tokenString.getBytes();
            passwordOrAPITokenBase64encoded = CryptoAccess.CRYPTO_STRING.seal(Base64.getEncoder().encodeToString(tokenBytes));
        }
        return CryptoAccess.CRYPTO_STRING.unseal(passwordOrAPITokenBase64encoded);
    }

    @Override
    public final String getUser() {
        return user;
    }

    @Override
    public String getPolicyId() {
        return policyId;
    }

    @Override
    public final String getPasswordOrAPIToken() {
        return CryptoAccess.CRYPTO_STRING.unseal(passwordOrAPIToken);
    }

    @Override
    public final String getProxyHostname() {
        return proxyHostname;
    }

    @Override
    public final int getProxyPort() {
        return proxyPort;
    }

    @Override
    public final boolean isProxyDefined() {
        return proxyHostname != null && !proxyHostname.isEmpty();
    }

    @Override
    public Map<AdapterOptionKey, String> getOptions() {
        return options;
    }

    @Override
    public String getProjectId() {
        return projectId;
    }

}
