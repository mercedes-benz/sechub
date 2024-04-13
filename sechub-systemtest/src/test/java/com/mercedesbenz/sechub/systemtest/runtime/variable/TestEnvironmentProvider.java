// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime.variable;

import java.util.HashMap;
import java.util.Map;

public class TestEnvironmentProvider implements EnvironmentProvider {

    private Map<String, String> map = new HashMap<>();

    public void setEnv(String variableName, String value) {
        map.put(variableName, value);
    }

    @Override
    public String getEnv(String variableName) {
        return map.get(variableName);
    }

}
