// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class PDSScriptEnvironmentCleaner {

    /**
     * Removes all environment variables from given environment map, except default
     * and explicit white listed variable names. Default variable names are inside
     * {@link PDSDefaulScriptEnvironmentVariableWhitelist}, set explicit white
     * listed names are set via {@link #setWhiteListCommaSeparated(String)}
     *
     * @param environment the environment map to clean
     */
    public void clean(Map<String, String> environment, Set<String> whiteList) {
        /* create backup */
        Map<String, String> backup = new HashMap<>();
        backup.putAll(environment);

        /* initial clear all */
        environment.clear();

        /* copy white listed parts back to environment */
        Iterator<String> variableNameIt = backup.keySet().iterator();

        while (variableNameIt.hasNext()) {
            String variableName = variableNameIt.next();
            if (isWhitelistedEnvironmentVariable(variableName, whiteList)) {
                String backupValue = backup.get(variableName);
                environment.put(variableName, backupValue);
            }
        }

    }

    private boolean isWhitelistedEnvironmentVariable(String variableName, Set<String> whiteList) {
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
