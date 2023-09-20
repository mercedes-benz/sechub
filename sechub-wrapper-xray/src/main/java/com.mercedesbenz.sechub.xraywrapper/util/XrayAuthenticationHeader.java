package com.mercedesbenz.sechub.xraywrapper.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class XrayAuthenticationHeader {
    public static String setAuthHeader() {
        EnvironmentVariableReader environmentVariableReader = new EnvironmentVariableReader();
        String username = environmentVariableReader.readEnvAsString(EnvironmentVariableConstants.XRAY_USERNAME_ENV);
        String pwd = environmentVariableReader.readEnvAsString(EnvironmentVariableConstants.XRAY_PASSWORD_ENV);
        String auth = (username + ":" + pwd);
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth);
    }
}
