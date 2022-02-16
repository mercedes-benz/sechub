// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

public class DefaultEnvironmentEntryProvider implements EnvironmentEntryProvider {

    @Override
    public String getEnvEntry(String name) {
        return System.getenv(name);
    }

}
