// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.resolve;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IPv4Pattern implements InetAddressPattern {

    private static final Logger LOG = LoggerFactory.getLogger(IPv4Pattern.class);

    private DividedStringPatternMatcher matcher;
    private boolean valid;

    public IPv4Pattern(String pattern) {
        matcher = new DividedStringPatternMatcher(pattern, '.');
        validate(pattern);
    }

    boolean isValid() {
        return valid;
    }

    private void validate(String pattern) {
        String[] parts = matcher.getPatternParts();
        this.valid = false;
        if (parts.length != 4) {
            LOG.debug("Pattern '{}' not valid for IPv4. Amount of dividers must be 4 and not {}.", pattern, parts.length);
            return;
        }
        for (String part : parts) {
            if ("*".contentEquals(part)) {
                continue;
            }
            try {
                int value = Integer.parseInt(part);
                if (value < 0 || value > 255) {
                    LOG.debug("Pattern '{}' not valid for IPv4. Number {} not valid. Please use 0-255 or * only", pattern, value);
                    return;
                }
            } catch (NumberFormatException e) {
                LOG.debug("Pattern '{}' not valid for IPv4. Allowed are 0-255 and * only", pattern);
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
