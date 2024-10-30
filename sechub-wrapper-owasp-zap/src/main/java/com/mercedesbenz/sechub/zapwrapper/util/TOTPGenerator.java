// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.util;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.mercedesbenz.sechub.commons.model.login.TOTPHashAlgorithm;

/**
 * https://datatracker.ietf.org/doc/html/rfc6238
 */
public class TOTPGenerator {

    private static final int BASE = 10;

    private static final int ONE_SECOND_IN_MILLISECONDS = 1000;

    private static final int DEFAULT_TOTP_LENGTH = 6;
    private static final int DEFAULT_TOKEN_VALIDITY_TIME_IN_SECONDS = 30;

    private String hashAlgorithmName;
    private int totpLength;
    private int tokenValidityTimeInSeconds;
    private long digitsTruncate;

    public TOTPGenerator() {
        this(DEFAULT_TOTP_LENGTH, TOTPHashAlgorithm.HMAC_SHA1, DEFAULT_TOKEN_VALIDITY_TIME_IN_SECONDS);
    }

    public TOTPGenerator(int totpLength, TOTPHashAlgorithm hashAlgorithm, int tokenValidityTimeInSeconds) {
        this.totpLength = totpLength;
        this.hashAlgorithmName = hashAlgorithm.getName();
        this.tokenValidityTimeInSeconds = tokenValidityTimeInSeconds;

        this.digitsTruncate = (long) Math.pow(BASE, this.totpLength);
    }

    /**
     * This method generates a TOTP from the seed (must be raw bytes no encoding)
     * and the current time stamp in milliseconds. Make sure encoded seeds like hex,
     * base32 or base64 are decoded before passing them to this method.
     *
     * @param seed
     * @param currentTimeMillis
     * @return
     */
    public String generateTOTP(byte[] seed, long currentTimeMillis) {
        if (seed == null) {
            throw new IllegalArgumentException("The specified seed must not be null!");
        }

        byte[] hash = computeHash(seed, currentTimeMillis);

        int offset = hash[hash.length - 1] & 0xf;
        /* @formatter:off */
        long binary = ((hash[offset] & 0x7f) << 24)
                   | ((hash[offset + 1] & 0xff) << 16)
                   | ((hash[offset + 2] & 0xff) << 8)
                   | (hash[offset + 3] & 0xff);
        /* @formatter:on */

        long otp = binary % digitsTruncate;
        // add prepended zeros if the otp does not match the wanted length
        return String.format("%0" + totpLength + "d", otp);
    }

    private byte[] computeHash(byte[] seed, long currentTimeMillis) {
        try {
            Mac mac = Mac.getInstance(hashAlgorithmName);
            mac.init(new SecretKeySpec(seed, hashAlgorithmName));
            byte[] timeBytes = computeTimeBytes(currentTimeMillis, tokenValidityTimeInSeconds);
            byte[] hash = mac.doFinal(timeBytes);
            return hash;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("The specified hash algorithm is unknown!", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("The specified seed was invalid!", e);
        }
    }

    private byte[] computeTimeBytes(long currentTimeMillis, int tokenValidityTimeInSeconds) {
        Long timeStep = (currentTimeMillis / ONE_SECOND_IN_MILLISECONDS) / tokenValidityTimeInSeconds;

        /* @formatter:off */
        byte[] timeBytes = ByteBuffer.allocate(Long.BYTES)
                                     .putLong(timeStep)
                                     .array();
        /* @formatter:on */
        return timeBytes;
    }

}
