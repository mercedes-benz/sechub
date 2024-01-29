// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class SecHubStatus {

    Map<String, String> statusInformation = new TreeMap<>();

    public Map<String, String> getStatusInformationMap() {
        return Collections.unmodifiableMap(statusInformation);
    }

    @Override
    public String toString() {
        return "SecHubStatus [" + (statusInformation != null ? "statusInformation=" + statusInformation : "") + "]";
    }

}
