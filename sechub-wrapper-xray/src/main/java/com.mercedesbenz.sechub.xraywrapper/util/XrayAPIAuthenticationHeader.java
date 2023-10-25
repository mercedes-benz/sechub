package com.mercedesbenz.sechub.xraywrapper.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperExitCode;
import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperRuntimeException;

public class XrayAPIAuthenticationHeader {

    /**
     * builds basic authentication header from username and password
     *
     * @return base64 encoded header for basic authentication
     */
    public static String buildAuthHeader() {
        EnvironmentVariableReader environmentVariableReader = new EnvironmentVariableReader();
        String username = environmentVariableReader.readEnvAsString(EnvironmentVariableConstants.XRAY_USERNAME_ENV);
        String pwd = environmentVariableReader.readEnvAsString(EnvironmentVariableConstants.XRAY_PASSWORD_ENV);
        String auth = (username + ":" + pwd);
        if (username == null || pwd == null) {
            throw new XrayWrapperRuntimeException("Authentication not possible because of missing environment variables XRAY_USER and XRAY_PASSWORD",
                    XrayWrapperExitCode.NOT_NULLABLE);
        }
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth);
    }
}
