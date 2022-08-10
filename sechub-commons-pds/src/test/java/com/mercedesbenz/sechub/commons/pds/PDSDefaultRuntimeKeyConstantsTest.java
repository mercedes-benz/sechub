package com.mercedesbenz.sechub.commons.pds;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

class PDSDefaultRuntimeKeyConstantsTest {

    /**
     * The test ensures that {@link PDSDefaultRuntimeKeyConstants} and
     * {@link PDSLauncherScriptEnvironmentConstants} are in sync. Wrapper
     * applications do not use the environment constants directly, but use spring
     * value injection where we need the keys. The environment variable constants
     * are used by PDS launcher script injects. <br>
     * <br>
     * When the test inspects problems, the output will contain code snippets which
     * can be directly added into source to fix the problem.
     *
     * @throws Exception
     */
    @Test
    void ensure_constants_contain_key_equivalent_to_launcher_script_constants() throws Exception {
        /* prepare */
        Map<String, String> runtimeKeys = fetchConstantsfromClass(PDSDefaultRuntimeKeyConstants.class);
        Map<String, String> environmentVariables = fetchConstantsfromClass(PDSLauncherScriptEnvironmentConstants.class);

        StringBuilder missingKeys = new StringBuilder();
        StringBuilder wrongKeys = new StringBuilder();
        StringBuilder additionalKeys = new StringBuilder();

        // start inspection
        for (String environmentVariableName : environmentVariables.keySet()) {
            String keyName = getKeyNameForVariableName(environmentVariableName);
            String value = runtimeKeys.get(keyName);
            String expectedValue = getKeyValueForVariableName(environmentVariableName);

            if (value == null) {
                missingKeys.append("public static final String " + keyName + "=\"" + expectedValue + "\";\n");
            } else if (!expectedValue.equals(value)) {
                wrongKeys.append(keyName + " should be:\n" + expectedValue + "\nbut was:\n" + value + "\n");
            }
        }

        if (runtimeKeys.size() != environmentVariables.size()) {
            /* check for additional keys - launcher script variables are leading ! */
            for (String key : runtimeKeys.keySet()) {
                String variableName = getVariableNameForKey(key);
                if (environmentVariables.get(variableName) == null) {
                    additionalKeys.append(key + " found, but there is no env variable:" + variableName + "\n");
                }

            }
        }
        // create problem message - if necessary
        StringBuilder problems = new StringBuilder();
        if (missingKeys.length() > 0) {
            problems.append(">>> Found missing keys:\n");
            problems.append(missingKeys);
        }
        if (wrongKeys.length() > 0) {
            problems.append(">>> Found wrong keys:\n");
            problems.append(wrongKeys);
        }
        if (additionalKeys.length() > 0) {
            problems.append(">>> Found additional keys:\n");
            problems.append(additionalKeys);
        }

        /* test */
        if (problems.length() > 0) {
            /* we dump to console as well, so easier to copy in IDE. */
            System.out.println(problems.toString());

            fail(problems.toString());
        }
    }

    private String getKeyNameForVariableName(String envName) {
        return "RT_KEY_" + envName;
    }

    private String getKeyValueForVariableName(String envName) {
        return envName.toLowerCase().replace('_', '.');
    }

    private String getVariableNameForKey(String key) {
        return key.toUpperCase().replace('.', '_').substring("RT_KEY_".length());
    }

    private Map<String, String> fetchConstantsfromClass(Class<?> clazz) throws Exception {
        Map<String, String> map = new TreeMap<>();
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            String name = field.getName();
            Object value = field.get(null);
            map.put(name, Objects.toString(value));
        }

        return map;

    }

}
