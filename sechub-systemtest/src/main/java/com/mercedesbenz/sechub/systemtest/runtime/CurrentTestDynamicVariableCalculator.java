package com.mercedesbenz.sechub.systemtest.runtime;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.mercedesbenz.sechub.systemtest.config.CalculatedVariables;
import com.mercedesbenz.sechub.systemtest.config.TestDefinition;

public class CurrentTestDynamicVariableCalculator implements DynamicVariableCalculator {

    private TestDefinition test;
    private SystemTestRuntimeContext context;

    public CurrentTestDynamicVariableCalculator(TestDefinition test, SystemTestRuntimeContext context) {
        this.test = test;
        this.context = context;
    }

    @Override
    public String calculateValue(String value) {
        if (value == null) {
            return null;
        }
        String result = value;
        String dynamicTestFolderExpression = CalculatedVariables.CURRENT_TEST_FOLDER.asExpression();
        int pos;
        while ((pos = result.indexOf(dynamicTestFolderExpression)) != -1) {
            int end = pos + dynamicTestFolderExpression.length();

            LocationSupport locationSupport = context.getLocationSupport();
            Path testFolder = locationSupport.ensureTestFolder(test);
            result = result.substring(0, pos) + testFolder.toString() + result.substring(end);

        }
        return result;
    }

    @Override
    public Map<String, String> calculateEnvironmentEntries(Map<String, String> origin) {
        Map<String, String> result = new TreeMap<>();
        for (String key : origin.keySet()) {
            String value = origin.get(key);

            String alteredKey = calculateValue(key);
            String alteredValue = calculateValue(value);

            result.put(alteredKey, alteredValue);
        }
        return result;
    }

    @Override
    public List<String> calculateArguments(List<String> list) {
        List<String> result = new ArrayList<>();
        for (String entry : list) {
            result.add(calculateValue(entry));
        }
        return result;
    }

}
