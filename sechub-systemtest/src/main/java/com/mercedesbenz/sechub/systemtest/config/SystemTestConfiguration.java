// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SystemTestConfiguration {

    private Map<String, String> variables = new LinkedHashMap<>();

    private SetupDefinition setup = new SetupDefinition();

    private List<TestDefinition> tests = new ArrayList<>();

    public SetupDefinition getSetup() {
        return setup;
    }

    public static SystemTestConfigurationBuilder builder() {
        return new SystemTestConfigurationBuilder();
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public List<TestDefinition> getTests() {
        return tests;
    }

}
