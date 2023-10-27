// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime.testengine;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.systemtest.config.CalculatedVariables;
import com.mercedesbenz.sechub.systemtest.config.TestDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.LocationSupport;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntimeContext;
import com.mercedesbenz.sechub.systemtest.runtime.variable.VariableCalculator;

public class CurrentTestVariableCalculator implements VariableCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(CurrentTestVariableCalculator.class);

    private TestDefinition test;

    private String testFolderPath;

    public CurrentTestVariableCalculator(TestDefinition test, SystemTestRuntimeContext context) {
        this.test = test;
        this.testFolderPath = resolveCurrentTestWorkingDirectoryPath(context);
    }

    private String resolveCurrentTestWorkingDirectoryPath(SystemTestRuntimeContext context) {
        LocationSupport locationSupport = context.getLocationSupport();
        Path testFolder = locationSupport.ensureTestWorkingDirectoryRealPath(test);
        return testFolder.toString();
    }

    @Override
    public String calculateValue(String value) {
        if (value == null) {
            return null;
        }
        String result = value;
        String testWorkingDirectoryExpression = CalculatedVariables.TEST_WORKING_DIRECTORY.asExpression();
        int pos;

        /*
         * currently we only provide the current test working directory here - in a very
         * simple way...
         */
        while ((pos = result.indexOf(testWorkingDirectoryExpression)) != -1) {
            int end = pos + testWorkingDirectoryExpression.length();

            result = result.substring(0, pos) + testFolderPath.toString() + result.substring(end);

        }
        LOG.trace("Calculated value: {} is replaced by {}", value, result);
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

    public String replace(String origin) {
        return calculateValue(origin);
    }

}
