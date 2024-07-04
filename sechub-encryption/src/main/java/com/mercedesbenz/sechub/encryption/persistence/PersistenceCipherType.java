// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.encryption.persistence;

/**
 * Contains the available ciphers.
 *
 * @author Jeremias Eppler
 */
public enum PersistenceCipherType {
    /**
     * A special cipher type which does not protect data.
     *
     * This is intended for testing.
     */
    NONE,

    /**
     * Advanced Encryption Standard (AES) 128 bit key (secret) in Galois/Counter
     * Mode (GCM) and synthetic initialization vector (SIV).
     *
     * AES GCM-SIV is a nonce (initialization vector) misuse-resistant authenticated
     * encryption.
     *
     * @see https://datatracker.ietf.org/doc/html/rfc8452
     */
    AES_GCM_SIV_128,

    /**
     * Advanced Encryption Standard (AES) 256 bit key (secret) in Galois/Counter
     * Mode (GCM) and synthetic initialization vector (SIV).
     *
     * AES GCM-SIV is a nonce (initialization vector) misuse-resistant authenticated
     * encryption.
     *
     * @see https://datatracker.ietf.org/doc/html/rfc8452
     */
    AES_GCM_SIV_256;
}
