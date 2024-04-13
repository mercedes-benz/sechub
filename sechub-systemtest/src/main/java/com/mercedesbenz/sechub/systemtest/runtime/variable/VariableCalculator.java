// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime.variable;

import java.util.List;
import java.util.Map;

public interface VariableCalculator {

    public String calculateValue(String value);

    public Map<String, String> calculateEnvironmentEntries(Map<String, String> origin);

    public List<String> calculateArguments(List<String> list);
}
