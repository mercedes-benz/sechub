// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.util;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.model.login.TOTPHashAlgorithm;

/**
 * https://datatracker.ietf.org/doc/html/rfc6238
 */
public class TOTPGenerator {

    private static final int BASE = 10;

    private static final int ONE_SECOND_IN_MILLISECONDS = 1000;

    private SealedObject seed;
    private String hashAlgorithmName;
    private int totpLength;
    private int tokenValidityTimeInSeconds;
    private long digitsTruncate;

    public TOTPGenerator(String seed, int totpLength, TOTPHashAlgorithm hashAlgorithm, int tokenValidityTimeInSeconds) {
        if (seed == null) {
            throw new IllegalArgumentException("The specified TOTP seed must not be null!");
        }

        this.seed = CryptoAccess.CRYPTO_STRING.seal(seed);
        this.totpLength = totpLength;
        this.hashAlgorithmName = hashAlgorithm.getName();
        this.tokenValidityTimeInSeconds = tokenValidityTimeInSeconds;

        this.digitsTruncate = (long) Math.pow(BASE, this.totpLength);
    }

    /**
     * This method generates a TOTP for the current times stamp in milliseconds.
     *
     * @return totp currently valid
     */
    public String now() {
        return generateTOTP(System.currentTimeMillis());
    }

    /**
     * This method generates a TOTP for a time stamp in milliseconds.
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
        return String.format("%0" + totpLength + "d", otp);
    }

    private byte[] computeHash(long currentTimeMillis) {
        try {
            Mac mac = Mac.getInstance(hashAlgorithmName);
            mac.init(new SecretKeySpec(CryptoAccess.CRYPTO_STRING.unseal(seed).getBytes(), hashAlgorithmName));
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
