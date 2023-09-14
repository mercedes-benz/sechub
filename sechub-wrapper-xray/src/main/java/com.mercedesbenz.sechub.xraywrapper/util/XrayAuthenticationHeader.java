package com.mercedesbenz.sechub.xraywrapper.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class XrayAuthenticationHeader {
    public static String setAuthHeader() {
        EnvironmentVairiableReader environmentVairiableReader = new EnvironmentVairiableReader();
        String username = environmentVairiableReader.readEnvAsString(EnvironmentVariableConstants.XRAY_USERNAME_ENV);
        String pwd = environmentVairiableReader.readEnvAsString(EnvironmentVariableConstants.XRAY_PASSWORD_ENV);
        String auth = (username + ":" + pwd);
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth);
    }
}
