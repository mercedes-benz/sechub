// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx.support;

public class CheckmarxOAuthData {
    String accessToken;
    long expiresInSeconds;
    String tokenType;
    long creationTimeMillis;

    public CheckmarxOAuthData() {
        this.creationTimeMillis = System.currentTimeMillis();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long calculateMillisecondsBeforeTokenExpires() {
        long currentTimeMillisEpoch = System.currentTimeMillis();
        long maximumAmountOfMillisecondsBeforeExpired = expiresInSeconds * 1000;

        long tokenExpiresInMillisEpoch = creationTimeMillis + maximumAmountOfMillisecondsBeforeExpired;

        long tokenExpiresInMilliseconds = tokenExpiresInMillisEpoch - currentTimeMillisEpoch;
        return tokenExpiresInMilliseconds;
    }
}