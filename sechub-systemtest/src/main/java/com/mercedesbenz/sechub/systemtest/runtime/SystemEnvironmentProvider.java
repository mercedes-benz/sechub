package com.mercedesbenz.sechub.systemtest.runtime;

public class SystemEnvironmentProvider implements EnvironmentProvider {

    @Override
    public String getEnv(String variableName) {
        return System.getenv(variableName);
    }

}
