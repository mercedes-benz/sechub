// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.resolve;

import java.net.InetAddress;

import org.springframework.stereotype.Component;

@Component
public class LoopbackAddressFinder {
    /* @formatter:off
	 * From: https://www.ietf.org/rfc/rfc2732.txt :
	 *
	 * To use a literal IPv6 address in a
	 * URL, the literal address should be enclosed in "[" and "]" characters. For
	 * example the following literal IPv6 addresses:
	 *
	 * FEDC:BA98:7654:3210:FEDC:BA98:7654:3210 1080:0:0:0:8:800:200C:4171
	 * 3ffe:2a00:100:7031::1 1080::8:800:200C:417A ::192.9.5.5 ::FFFF:129.144.52.38
	 * 2010:836B:4179::836B:4179
	 *
	 * would be represented as in the following example URLs:
	 *
	 * http://[FEDC:BA98:7654:3210:FEDC:BA98:7654:3210]:80/index.html
	 * http://[1080:0:0:0:8:800:200C:417A]/index.html http://[3ffe:2a00:100:7031::1]
	 * http://[1080::8:800:200C:417A]/foo http://[::192.9.5.5]/ipng
	 * http://[::FFFF:129.144.52.38]:80/index.html
	 * http://[2010:836B:4179::836B:4179]
	 *
	 * Loop back variants
     * Syntax:
	 * see https://en.wikipedia.org/wiki/Loopback
	 * see https://tools.ietf.org/html/rfc4291
	 *
	 * @formatter:on
	 */
    public boolean isLoopback(String ipAddressAsString) {
        if (ipAddressAsString == null) {
            return false;
        }
        String ipAddress = ipAddressAsString.trim();
        if (ipAddressAsString.isEmpty()) {
            return false;
        }
        if (ipAddress.contentEquals("localhost")) {
            return true;
        }
        if (ipAddress.startsWith("127.")) {
            return true;
        }
        if (!ipAddress.startsWith("[")) {
            /* no IPv6 parts, so all valid at this point */
            return false;
        }
        /* IPv6 parts: */
        if (ipAddress.equals("[::1]")) {
            return true;
        }
        if (ipAddress.equals("[0:0:0:0:0:0:0:1]")) {
            return true;
        }
        return false;
    }

    public boolean isLoopback(InetAddress ip) {
        if (ip == null) {
            return false;
        }
        return ip.isLoopbackAddress();
    }
}
