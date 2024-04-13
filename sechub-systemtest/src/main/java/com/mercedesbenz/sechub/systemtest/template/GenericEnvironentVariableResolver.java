// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.template;

import java.util.Collections;
import java.util.List;

import com.mercedesbenz.sechub.systemtest.runtime.variable.EnvironmentProvider;

class GenericEnvironentVariableResolver implements VariableValueResolver {
    private EnvironmentProvider environmentProvider;
    private TemplateVariableType type;

    GenericEnvironentVariableResolver(EnvironmentProvider environmentProvider, TemplateVariableType type) {
        this.environmentProvider = environmentProvider;
        this.type = type;
    }

    @Override
    public String resolveValueFor(String variableName) {
        String envEntry = environmentProvider.getEnv(variableName);
        if (envEntry == null) {
            return "";
        }
        return envEntry;
    }

    @Override
    public TemplateVariableType getType() {
        return type;
    }

    @Override
    public List<String> createProposals() {
        return Collections.emptyList();
    }

}