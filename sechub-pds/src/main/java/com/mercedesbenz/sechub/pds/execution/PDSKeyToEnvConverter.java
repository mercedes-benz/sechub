// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
/**
 * This class provides functionality to convert keys to environment variable
 * format. It replaces dots with underscores and converts the string to
 * uppercase. Additionally, it removes any hyphens from the key.
 */
public class PDSKeyToEnvConverter {

    private static final Pattern P = Pattern.compile("\\.");

    /**
     * Converts the given key to an environment variable format. The conversion
     * process includes replacing dots with underscores, converting to uppercase,
     * and removing hyphens.
     *
     * @param key the key to be converted. If the key is <code>null</code>,
     *            <code>null</code> is returned.
     * @return the converted key in environment variable format, or
     *         <code>null</code> if the input key is <code>null</code>.
     */
    public String convertKeyToEnv(String key) {
        if (key == null) {
            return null;
        }
        String result = P.matcher(key).replaceAll("_").toUpperCase();
        if (result.indexOf('-') != -1) {
            result = result.replaceAll("-", "");
        }
        return result;
    }
}
