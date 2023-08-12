// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class GenericMapVariableResolver implements VariableValueResolver {

    private Map<String, String> variables;
    private TemplateVariableType type;

    GenericMapVariableResolver(Map<String, String> variables, TemplateVariableType prefix) {
        this.variables = variables;
        this.type = prefix;
    }

    @Override
    public String resolveValueFor(String variableName) {
        return variables.get(variableName);
    }

    @Override
    public TemplateVariableType getType() {
        return type;
    }

    @Override
    public List<String> createProposals() {
        List<String> candidates = new ArrayList<>();
        for (String key : variables.keySet()) {
            candidates.add(type.getFullPrefix() + key);
        }
        return candidates;
    }

}