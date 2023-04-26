package com.mercedesbenz.sechub.systemtest.runtime;

import java.util.List;
import java.util.Map;

public interface DynamicVariableCalculator {

    public String calculateValue(String value);

    public Map<String, String> calculateEnvironmentEntries(Map<String, String> origin);

    public List<String> calculateArguments(List<String> list);
}
