// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.util;

public class EnvironmentVariableToSystemPropertyConverter {

    public String convertEnvironmentVariableToSystemPropertyKey(String from) {
        if (from == null) {
            return null;
        }
        String result = from.replaceAll("_", ".");
        return result.toLowerCase();
    }

}
