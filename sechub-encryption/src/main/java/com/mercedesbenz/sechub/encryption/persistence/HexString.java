// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.encryption.persistence;

import java.util.HexFormat;

/**
 * Hexadecimal encoded string.
 *
 * @author Jeremias Eppler
 */
public class HexString extends AbstractBinaryString {

    protected HexFormat hexFormat = HexFormat.of();

    HexString(byte[] bytes) {
        super(bytes);
    }

    HexString(String string) {
        super(string);
    }

    @Override
    public String toString() {
        return hexFormat.formatHex(bytes);
    }

    @Override
    public BinaryStringEncodingType getType() {
        return BinaryStringEncodingType.HEX;
    }

}
