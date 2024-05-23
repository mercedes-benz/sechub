// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PDSScriptEnvironmentCleaner {

    private Set<String> whiteList = new HashSet<>();

    public void clean(Map<String, String> environment) {
        /* create backup */
        Map<String, String> backup = new HashMap<>();
        backup.putAll(environment);

        /* initial clear all */
        environment.clear();

        /* copy white listed parts back to environment */
        Iterator<String> variableNameIt = backup.keySet().iterator();

        while (variableNameIt.hasNext()) {
            String variableName = variableNameIt.next();
            if (isWhitelistedEnvironmentVariable(variableName)) {
                environment.put(variableName, backup.get(variableName));
            }
        }

    }

    public void setWhiteListCommaSeparated(String commaSeparatedWhiteList) {
        whiteList.clear();

        if (commaSeparatedWhiteList == null || commaSeparatedWhiteList.isBlank()) {
            return;
        }

        String[] splitted = commaSeparatedWhiteList.split(",");
        for (String whiteListEntry : splitted) {
            String trimmed = whiteListEntry.trim();
            if (trimmed.isBlank()) {
                continue;
            }
            whiteList.add(trimmed);
        }
    }

    private boolean isWhitelistedEnvironmentVariable(String key) {
        if (key == null) {
            return false;
        }

        /* handle default white list entries */
        for (PDSScriptEnvironmentVariableWhitelistDefault defaultKey : PDSScriptEnvironmentVariableWhitelistDefault.values()) {
            if (defaultKey.name().equals(key)) {
                return true;
            }
        }

        /* handle explicit white list entries */
        if (whiteList.contains(key)) {
            return true;
        }

        /* handle asterisk variants */
        for (String whiteListEntry : whiteList) {
            int length = whiteListEntry.length();

            if (whiteListEntry.endsWith("*") && length > 2) {
                String prefix = whiteListEntry.substring(0, length - 1);
                if (key.startsWith(prefix)) {
                    return true;
                }
            }
        }

        /* not white listed */
        return false;
    }

}
