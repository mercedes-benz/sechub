// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.execution;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class PDSKeyToEnvConverter {

    private static final Pattern P = Pattern.compile("\\.");

    public String convertKeyToEnv(String key) {
        if (key == null) {
            return null;
        }
        return P.matcher(key).replaceAll("_").toUpperCase();
    }
}
