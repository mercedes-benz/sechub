// SPDX-License-Identifier: MIT
package com.daimler.sechub.test;

public class DefaultEnvironmentEntryProvider implements EnvironmentEntryProvider {

    @Override
    public String getEnvEntry(String name) {
        return System.getenv(name);
    }

}
