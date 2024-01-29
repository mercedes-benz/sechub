// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.template;

import java.util.List;

interface VariableValueResolver {

    public String resolveValueFor(String variableName);

    public TemplateVariableType getType();

    /**
     * Creates a list of all proposals (with full variable names). If the
     * implementation does not support this an empty list is returned.
     *
     * @return list, never <code>null</code>
     */
    public List<String> createProposals();

}