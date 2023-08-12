// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime.variable;

public class SystemEnvironmentProvider implements EnvironmentProvider {

    @Override
    public String getEnv(String variableName) {
        return System.getenv(variableName);
    }

}
