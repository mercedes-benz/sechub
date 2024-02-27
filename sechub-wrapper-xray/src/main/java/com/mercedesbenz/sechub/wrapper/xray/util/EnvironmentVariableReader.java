// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.util;

public class EnvironmentVariableReader {

    public String readEnvAsString(String environmentVariable) {
        return System.getenv(environmentVariable);
    }
}
