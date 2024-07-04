// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.security.persistence;

import java.util.Base64;
import java.util.HexFormat;

/**
 * Creates {@link BinaryString} differently encoded strings.
 *
 * It can create {@link BinaryString} from differently encoded strings and
 * returns a sub-type of {@link BinaryString} based on the desired output
 * encoding.
 *
 * @author Jeremias Eppler
 */
public class BinaryStringFactory {
    private static final BinaryStringEncodingType DEFAULT_ENCODING_TYPE = BinaryStringEncodingType.BASE64;

    public static BinaryString createFromBytes(byte[] bytes) {
        return createFromBytes(bytes, DEFAULT_ENCODING_TYPE);
    }

    public static BinaryString createFromBytes(byte[] bytes, BinaryStringEncodingType encodingType) {
        if (bytes == null) {
            throw new IllegalArgumentException("String cannot be null.");
        }

        BinaryString binaryString;

        switch (encodingType) {
        case BASE64:
            binaryString = new Base64String(bytes);
            break;
        case HEX:
            binaryString = new HexString(bytes);
            break;
        case PLAIN:
            binaryString = new PlainString(bytes);
            break;
        default:
            throw new IllegalArgumentException("Unknown binary string type");
        }

        return binaryString;
    }

    /**
     * Creates a {@link BinaryString} from a plain Java string.
     *
     * The BinaryString will be in the default encoding.
     *
     * @param string
     * @return
     */
    public static BinaryString createFromString(String string) {
        return createFromString(string, DEFAULT_ENCODING_TYPE);
    }

    /**
     * Creates a {@link BinaryString} from a plain Java string.
     *
     * It returns the string in the specified encoding.
     *
     * @param string
     * @param encodingType
     * @return
     */
    public static BinaryString createFromString(String string, BinaryStringEncodingType encodingType) {
        if (string == null) {
            throw new IllegalArgumentException("String cannot be null.");
        }

        BinaryString binaryString;

        switch (encodingType) {
        case BASE64:
            binaryString = new Base64String(string);
            break;
        case HEX:
            binaryString = new HexString(string);
            break;
        case PLAIN:
            binaryString = new PlainString(string);
            break;
        default:
            throw new IllegalArgumentException("Unknown binary string type");
        }

        return binaryString;
    }

    /**
     * Create a new {@link BinaryString} from a string which is in hexadecimal
     * encoding.
     *
     * The given string needs to be hexadecimal encoded.
     *
     * For example: Given the string "616263" (orginal value "abc") it will decode
     * it and store it as bytes.
     *
     * @param stringInHexFormat
     * @param encodingType
     * @return
     */
    public static BinaryString createFromHex(String stringInHexFormat) {
        return createFromHex(stringInHexFormat, DEFAULT_ENCODING_TYPE);
    }

    /**
     * Create a new {@link BinaryString} from a string which is in hexadecimal
     * encoding.
     *
     * The given string needs to be hexadecimal encoded.
     *
     * For example: Given the string "616263" (orginal value "abc") it will decode
     * it and store it as bytes.
     *
     * @param stringInHexFormat
     * @param encodingType
     * @return
     */
    public static BinaryString createFromHex(String stringInHexFormat, BinaryStringEncodingType encodingType) {
        if (stringInHexFormat == null) {
            throw new IllegalArgumentException("String cannot be null.");
        }

        HexFormat hexFormat = HexFormat.of();
        byte[] hexBytes = hexFormat.parseHex(stringInHexFormat);

        return createFromBytes(hexBytes, encodingType);
    }

    /**
     * Create a new {@link BinaryString} from a string which is base64 encoded.
     *
     * The given string needs to be base64 encoded.
     *
     * For example: Given the string "YWJj" (orginal value "abc") it will decode it
     * and store it as bytes.
     *
     * @param stringInBase64Format
     * @return
     */
    public static BinaryString createFromBase64(String stringInBase64Format) {
        return createFromBase64(stringInBase64Format, DEFAULT_ENCODING_TYPE);
    }

    /**
     * Create a new {@link BinaryString} from a string which is base64 encoded.
     *
     * The given string needs to be base64 encoded.
     *
     * For example: Given the string "YWJj" (orginal value "abc") it will decode it
     * and store it as bytes.
     *
     * @param stringInBase64Format
     * @param encodingType
     * @return
     */
    public static BinaryString createFromBase64(String stringInBase64Format, BinaryStringEncodingType encodingType) {
        if (stringInBase64Format == null) {
            throw new IllegalArgumentException("String cannot be null.");
        }

        // if it cannot be decoded,
        // it will throw an IllegalArgumentException
        byte[] decoded = Base64.getDecoder().decode(stringInBase64Format);

        return createFromBytes(decoded, encodingType);
    }
}
