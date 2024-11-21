// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.util;

import java.util.regex.Pattern;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import com.mercedesbenz.sechub.commons.model.login.EncodingType;

public class StringDecoder {

    private static final String HEX_REGEX = "^(0x|#)?[\\p{XDigit}]+$";
    private static final Pattern HEX_PATTERN = Pattern.compile(HEX_REGEX);

    private static final String BASE32_REGEX = "^[A-Z2-7]+=*$";
    private static final Pattern BASE32_PATTERN = Pattern.compile(BASE32_REGEX);

    private static final String BASE64_REGEX = "^[-A-Za-z0-9+/]+=*$";
    private static final Pattern BASE64_PATTERN = Pattern.compile(BASE64_REGEX);

    public byte[] decodeIfNecessary(String seed, EncodingType encodingType) {
        if (seed == null) {
            throw new IllegalArgumentException("The secret key must not be null!");
        }
        // some services generate keys with spaces
        seed = seed.replaceAll("\\s", "");

        EncodingType realEncodingType = encodingType;
        if (realEncodingType == null || realEncodingType == EncodingType.AUTODETECT) {
            realEncodingType = detectEncoding(seed);
        }

        try {
            switch (realEncodingType) {
            case HEX:
                return Hex.decodeHex(seed);
            case BASE32:
                Base32 base32 = new Base32();
                return base32.decode(seed);
            case BASE64:
                return Base64.decodeBase64(seed);
            case PLAIN:
            default:
                return seed.getBytes();
            }
        } catch (DecoderException e) {
            throw new IllegalArgumentException("The secret key could not be decoded!", e);
        }
    }

    /**
     * This detection can produce errors of the regex and the length matches with a
     * human readable string as input. In the context of secret keys which are
     * usually random it should be enough to handle the encodings correctly.
     *
     * Normally a service generates a random secret key for TOTP and nothing like
     * 'example1', which technically would match the condition for BASE64 below.
     *
     * @param string
     * @return
     */
    private EncodingType detectEncoding(String string) {
        if (HEX_PATTERN.matcher(string).matches()) {
            return EncodingType.HEX;
        }
        if (BASE32_PATTERN.matcher(string).matches()) {
            // base32 string length is always a multiple of 8
            if (string.length() % 8 == 0) {
                return EncodingType.BASE32;
            }
        }
        if (BASE64_PATTERN.matcher(string).matches()) {
            // base64 string length is always a multiple of 4
            if (string.length() % 4 == 0) {
                return EncodingType.BASE64;
            }
        }
        return EncodingType.PLAIN;
    }

}
