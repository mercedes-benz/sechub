// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

/**
 * A specialized byte transformer which transform String to byte array a vice
 * versa with same character encoding (UTF-8).
 *
 * @author Albert Tregnaghi
 *
 */
public class StringByteArrayTransformer implements ByteArrayTransformer<String> {

    @Override
    public String transformFromBytes(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return new String(bytes, EncryptionConstants.UTF8_CHARSET_ENCODING);
    }

    @Override
    public byte[] transformToBytes(String string) {
        if (string == null) {
            return null;
        }
        return string.getBytes(EncryptionConstants.UTF8_CHARSET_ENCODING);
    }

}
