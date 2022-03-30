// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.resolve;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IPv6Pattern implements InetAddressPattern {

    private static final Logger LOG = LoggerFactory.getLogger(IPv4Pattern.class);

    private DividedStringPatternMatcher matcher;
    private boolean valid;

    public IPv6Pattern(String pattern) {
        matcher = new DividedStringPatternMatcher(pattern, ':');
        validate(pattern);
    }

    boolean isValid() {
        return valid;
    }

    private void validate(String pattern) {
        String[] parts = matcher.getPatternParts();
        this.valid = false;
        if (parts.length != 8) {
            /*
             * we do NOT networkTargetDataSupport short form but only long variant, which
             * has always 8 parts
             */
            LOG.debug(
                    "Pattern '{}' not valid for IPv6 (we do not provide compact form for patterns, only long variant). Amount of dividers must be 8 and not {}.",
                    pattern, parts.length);
            return;
        }
        for (String part : parts) {
            if ("*".contentEquals(part)) {
                continue;
            }
            try {
                int value = Integer.decode("0x" + part); // its hex code 2 bytes
                if (value < 0 || value > 0xffff) {
                    LOG.debug("Pattern '{}' not valid for IPv6. Number {} not valid. Please use 0-FFFF or * only", pattern, value);
                    return;
                }
            } catch (NumberFormatException e) {
                LOG.debug("Pattern '{}' not valid for IPv6. Allowed are 0-FFF and * only", pattern);
                return;
            }
        }
        valid = true;

    }

    public boolean isMatching(InetAddress address) {
        if (!valid) {
            return false;
        }
        return matcher.isMatching(address.getHostAddress());
    }
}
