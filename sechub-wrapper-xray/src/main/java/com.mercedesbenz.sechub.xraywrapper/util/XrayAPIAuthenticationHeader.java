package com.mercedesbenz.sechub.xraywrapper.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class XrayAPIAuthenticationHeader {

    /**
     * builds basic authentication header from username and password
     *
     * @return basic encoded authentication header
     */
    public static String buildAuthHeader() {
        EnvironmentVariableReader environmentVariableReader = new EnvironmentVariableReader();
        String username = environmentVariableReader.readEnvAsString(EnvironmentVariableConstants.XRAY_USERNAME_ENV);
        String pwd = environmentVariableReader.readEnvAsString(EnvironmentVariableConstants.XRAY_PASSWORD_ENV);
        String auth = (username + ":" + pwd);
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth);
    }
}
