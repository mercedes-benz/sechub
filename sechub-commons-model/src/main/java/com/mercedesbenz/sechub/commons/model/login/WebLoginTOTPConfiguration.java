// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.login;

import javax.crypto.SealedObject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebLoginTOTPConfiguration {

    public static final String PROPERTY_SEED = "seed";
    public static final String PROPERTY_VALIDITY_IN_SECONDS = "validityInSeconds";
    public static final String PROPERTY_TOKEN_LENGTH = "tokenLength";
    public static final String PROPERTY_HASH_ALGORITHM = "hashAlgorithm";

    private static final int DEFAULT_VALIDITY_IN_SECONDS = 30;
    private static final int DEFAULT_TOKEN_LENGTH = 6;
    private static final TOTPHashAlgorithm DEFAULT_HASH_ALGORITHM = TOTPHashAlgorithm.HMAC_SHA1;

    private SealedObject seed;
    private int validityInSeconds;
    private int tokenLength;
    private TOTPHashAlgorithm hashAlgorithm;

    public WebLoginTOTPConfiguration() {
        this.validityInSeconds = DEFAULT_VALIDITY_IN_SECONDS;
        this.tokenLength = DEFAULT_TOKEN_LENGTH;
        this.hashAlgorithm = DEFAULT_HASH_ALGORITHM;
    }

    public String getSeed() {
        return CryptoAccess.CRYPTO_STRING.unseal(seed);
    }

    public void setSeed(String seed) {
        this.seed = CryptoAccess.CRYPTO_STRING.seal(seed);
    }

    public int getValidityInSeconds() {
        return validityInSeconds;
    }

    public void setValidityInSeconds(int validityInSeconds) {
        this.validityInSeconds = validityInSeconds;
    }

    public int getTokenLength() {
        return tokenLength;
    }

    public void setTokenLength(int tokenLength) {
        this.tokenLength = tokenLength;
    }

    public TOTPHashAlgorithm getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(TOTPHashAlgorithm hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

}
