// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo;

import java.util.Random;

import org.springframework.stereotype.Component;

@Component
public class SkopeoLocationConverter {

    private static final String PROTOCOL_SPLIT = "://";

    private static final String DEFAULT_TAG_PREFIX = "default-tag";

    public String convertLocationForDownload(String location) {
        return "docker://" + removeProtocolPrefix(location);
    }

    public String convertLocationForLogin(String location) {
        return removeProtocolPrefix(location);
    }

    /**
     * Converts a location to an additional tag. If the location is empty or null, a
     * default tag is created.
     *
     * @param location the location to convert
     * @return the location without protocol prefix or default tag
     */
    public String convertLocationForAdditionalTag(String location) {
        String withoutProtocol = removeProtocolPrefix(location);
        if (withoutProtocol.isBlank() || withoutProtocol.isEmpty()) {
            // we add a random number to prevent conflicts during scans
            withoutProtocol = DEFAULT_TAG_PREFIX + ":" + new Random().nextInt(1000);
        }
        return withoutProtocol;
    }

    private String removeProtocolPrefix(String location) {
        if (location == null || location.isEmpty()) {
            return location;
        }
        int index = location.indexOf(PROTOCOL_SPLIT);
        if (index == -1) {
            return location;
        }
        int pos = index + PROTOCOL_SPLIT.length();
        if (location.length() < pos + 1) {
            return location;
        }
        String withoutPrefix = location.substring(pos);

        return withoutPrefix.split("/")[0];
    }

}
