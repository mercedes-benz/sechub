// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

public class TestEnvironmentProvider implements EnvironmentEntryProvider, SystemPropertyProvider {

    @Override
    public String getEnvEntry(String name) {
        return System.getenv(name);
    }

    @Override
    public String getSystemProperty(String name) {
        return System.getProperty(name);
    }

}
