package com.mercedesbenz.sechub.xraywrapper.util;

public class EnvironmentVariableReader {
    public String readEnvAsString(String environmentVariable) {

        return System.getenv(environmentVariable);
    }

}
