// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo;

import org.springframework.stereotype.Component;

@Component
public class SkopeoLocationConverter {

    private static final String PROTOCOL_SPLIT = "://";

    public String convertLocationForDownload(String location) {
        return "docker://" + removeProtocolPrefix(location);
    }

    public String convertLocationForLogin(String location) {
        return removeProtocolPrefix(location);
    }

    public String removeProtocolPrefix(String location) {
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
