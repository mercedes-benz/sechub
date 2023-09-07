// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime.variable;

import java.util.List;
import java.util.Map;

public class KeepAsIsDynamicVariableCalculator implements VariableCalculator {

    @Override
    public String calculateValue(String value) {
        return value;
    }

    @Override
    public Map<String, String> calculateEnvironmentEntries(Map<String, String> origin) {
        return origin;
    }

    @Override
    public List<String> calculateArguments(List<String> list) {
        return list;
    }

}
