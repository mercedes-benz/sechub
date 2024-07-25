// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo;

import org.springframework.stereotype.Component;

@Component
public class SkopeoLocationConverter {

    private static final String PROTOCOL_SPLIT = "://";

    public String convertLocationToDockerDownloadURL(String location) {
        return "docker://" + removeProtocolPrefix(location);
    }

    public String convertLocationToLoginLocation(String location) {
        return removeProtocolPrefix(location);
    }

    /**
     * Converts a location to an additional tag. If the location is empty or null, a
     * default tag is created.
     *
     * @param location the location to convert
     * @return the location without protocol prefix or default tag
     */
    public String convertLocationToAdditionalTag(String location) {
        String withoutProtocol = removeProtocolPrefix(location);
        if (withoutProtocol.isBlank() || withoutProtocol.isEmpty() || withoutProtocol == null) {
            // we add a random number to prevent conflicts during scans
            throw new IllegalStateException("Could not set additional tag for skopeo location.");
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
