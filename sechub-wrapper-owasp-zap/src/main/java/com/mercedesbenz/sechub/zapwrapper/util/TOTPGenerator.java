// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.util;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.mercedesbenz.sechub.commons.model.login.WebLoginTOTPConfiguration;

/**
 * https://datatracker.ietf.org/doc/html/rfc6238
 */
public class TOTPGenerator {

    private static final int BASE = 10;

    private static final int ONE_SECOND_IN_MILLISECONDS = 1000;

    private final WebLoginTOTPConfiguration totpConfig;
    private final long digitsTruncate;
    private final Mac mac;

    public TOTPGenerator(WebLoginTOTPConfiguration totpConfig) {
        this.totpConfig = assertValidTotpConfig(totpConfig);

        this.digitsTruncate = (long) Math.pow(BASE, totpConfig.getTokenLength());

        String hashAlgorithmName = totpConfig.getHashAlgorithm().getName();
        try {
            this.mac = Mac.getInstance(hashAlgorithmName);
            byte[] rawSeedBytes = new ZapWrapperStringDecoder().decodeIfNecessary(totpConfig.getSeed(), totpConfig.getEncodingType());
            mac.init(new SecretKeySpec(rawSeedBytes, hashAlgorithmName));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("The specified TOTP hash algorithm: '" + hashAlgorithmName + "' is unknown!", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("The specified TOTP seed was invalid!", e);
        }
    }

    /**
     * This method generates a TOTP for the current timestamp in milliseconds.
     *
     * @return totp currently valid
     */
    public String now() {
        return generateTOTP(System.currentTimeMillis());
    }

    /**
     * This method generates a TOTP for a timestamp in milliseconds.
     *
     * @param seed
     * @param currentTimeMillis
     * @return totp of give timestamp
     */
    public String generateTOTP(long currentTimeMillis) {
        byte[] hash = computeHash(currentTimeMillis);

        int offset = hash[hash.length - 1] & 0xf;
        /* @formatter:off */
        long binary = ((hash[offset] & 0x7f) << 24)
                   | ((hash[offset + 1] & 0xff) << 16)
                   | ((hash[offset + 2] & 0xff) << 8)
                   | (hash[offset + 3] & 0xff);
        /* @formatter:on */

        long otp = binary % digitsTruncate;
        // add prepended zeros if the otp does not match the wanted length
        return String.format("%0" + totpConfig.getTokenLength() + "d", otp);
    }

    private byte[] computeHash(long currentTimeMillis) {
        byte[] timeBytes = computeTimeBytes(currentTimeMillis);
        byte[] hash = mac.doFinal(timeBytes);
        return hash;
    }

    private byte[] computeTimeBytes(long currentTimeMillis) {
        Long timeStep = (currentTimeMillis / ONE_SECOND_IN_MILLISECONDS) / totpConfig.getValidityInSeconds();

        /* @formatter:off */
        byte[] timeBytes = ByteBuffer.allocate(Long.BYTES)
                                     .putLong(timeStep)
                                     .array();
        /* @formatter:on */
        return timeBytes;
    }

    /**
     * This method checks if the TOTP configuration does not contain any
     * <code>null</code> values. It is necessary if the TOTPGenerator is used with
     * WebLoginTOTPConfiguration, that are not provided by SecHub.
     *
     * The TOTP configuration provided by SecHub, should never contain
     * <code>null</code> values, because SecHub already validates the SecHub TOTP
     * configuration. For validation details see:
     * {@link com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidator}
     *
     * @param totpConfig
     * @return totpConfig, only if it is valid.
     *
     * @throws IllegalArgumentException if the the parameter totpConfig is invalid.
     */
    private WebLoginTOTPConfiguration assertValidTotpConfig(WebLoginTOTPConfiguration totpConfig) {
        if (totpConfig == null) {
            throw new IllegalArgumentException("The TOTP configuration must not be null!");
        }
        if (totpConfig.getSeed() == null) {
            throw new IllegalArgumentException("The TOTP configuration seed must not be null!");
        }
        if (totpConfig.getHashAlgorithm() == null) {
            throw new IllegalArgumentException("The TOTP configuration hash algorithm must not be null!");
        }
        if (totpConfig.getEncodingType() == null) {
            throw new IllegalArgumentException("The TOTP configuration encoding type must not be null!");
        }
        return totpConfig;
    }

}
