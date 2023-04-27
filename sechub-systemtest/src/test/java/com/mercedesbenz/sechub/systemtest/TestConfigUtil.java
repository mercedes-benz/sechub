package com.mercedesbenz.sechub.systemtest;

import static com.mercedesbenz.sechub.systemtest.TestConfigConstants.*;

public class TestConfigUtil {

    public static int getSecHubIntTestServerPort() {
        return fetchIntegerFromPropertyOrDefault(SYSTEM_PROPERTY_SECHUB_INTTEST_PORT, DEFAULT_SECHUB_INTTEST_PORT);
    }

    public static int getPDSIntTestServerPort() {
        return fetchIntegerFromPropertyOrDefault(SYSTEM_PROPERTY_PDS_INTTEST_PORT, DEFAULT_PDS_INTTEST_PORT);
    }

    private static int fetchIntegerFromPropertyOrDefault(String id, int defaultValue) {
        String sechubPortAsString = System.getProperty(id);

        int value = defaultValue;
        try {
            value = Integer.parseInt(sechubPortAsString);
        } catch (NumberFormatException e) {
            /* ignore - we use default */
        }
        return value;
    }

}
