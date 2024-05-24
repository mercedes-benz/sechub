// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PDSScriptEnvironmentCleaner {

    private Set<String> whiteList = new HashSet<>();

    /**
     * Removes all environment variables from given environment map, except default
     * and explicit white listed variable names. Default variable names are inside
     * {@link PDSDefaulScriptEnvironmentVariableWhitelist}, set explicit white
     * listed names are set via {@link #setWhiteListCommaSeparated(String)}
     *
     * @param environment the environment map to clean
     */
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
                String backupValue = backup.get(variableName);
                environment.put(variableName, backupValue);
            }
        }

    }

    /**
     * Sets the white list as a comma separated list of variable names to exclude
     * from cleaning. If a variable name ends with an asterisk every variable which
     * begins with such prefix will be accepted. For example: PDS_STORAGE_* will
     * white list any kind of environment variable which starts with PDS_STORAGE_
     * (e.g. PDS_STORAGE_S3_USER).
     *
     * @param commaSeparatedWhiteList comma separated list of white list entries.
     */
    public void setWhiteListCommaSeparated(String commaSeparatedWhiteList) {
        whiteList.clear();

        if (commaSeparatedWhiteList == null || commaSeparatedWhiteList.isBlank()) {
            return;
        }

        String[] splitted = commaSeparatedWhiteList.split(",");
        for (String whiteListEntry : splitted) {
            String trimmedWhiteListEntry = whiteListEntry.trim();
            if (trimmedWhiteListEntry.isBlank()) {
                continue;
            }
            whiteList.add(trimmedWhiteListEntry);
        }
    }

    private boolean isWhitelistedEnvironmentVariable(String variableName) {
        if (variableName == null) {
            return false;
        }

        /* handle default white list entries */
        for (PDSDefaulScriptEnvironmentVariableWhitelist defaultWhitelistVariable : PDSDefaulScriptEnvironmentVariableWhitelist.values()) {
            if (defaultWhitelistVariable.name().equals(variableName)) {
                return true;
            }
        }

        /* handle explicit white list entries */
        if (whiteList.contains(variableName)) {
            return true;
        }

        /* handle asterisk variants */
        for (String whiteListEntry : whiteList) {
            int length = whiteListEntry.length();

            if (whiteListEntry.endsWith("*") && length > 2) {
                String prefix = whiteListEntry.substring(0, length - 1);
                if (variableName.startsWith(prefix)) {
                    return true;
                }
            }
        }

        /* not white listed */
        return false;
    }

}
