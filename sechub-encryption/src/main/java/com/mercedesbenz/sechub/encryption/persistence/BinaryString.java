// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.encryption.persistence;

/**
 * A binary string type which knows it's encoding.
 *
 * The binary string keeps it's internal representation in binary format. In
 * addition, the binary string knows it's encoding (e.g. base64 etc.).
 *
 * @see BinaryStringEncodingType
 *
 * @author Jeremias Eppler
 */
public interface BinaryString {
    public byte[] getBytes();

    public String toString();

    public BinaryStringEncodingType getType();
}
