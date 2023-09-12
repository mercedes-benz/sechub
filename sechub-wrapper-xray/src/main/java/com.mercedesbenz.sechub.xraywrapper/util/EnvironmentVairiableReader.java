package com.mercedesbenz.sechub.xraywrapper.util;

public class EnvironmentVairiableReader {
    public String readEnvAsString(String environmentVariable) {
        return System.getenv(environmentVariable);
    }

}
