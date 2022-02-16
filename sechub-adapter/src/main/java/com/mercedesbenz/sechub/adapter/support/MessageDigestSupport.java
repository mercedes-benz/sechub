// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.support;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class MessageDigestSupport {

    public String createMD5(String value) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            String charset = StandardCharsets.UTF_8.name();
            md5.update(value.getBytes(charset));

            byte[] digest = md5.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot creaete MD5 hash", e);
        }
    }

}
