// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;

public class XrayAPIAuthenticationHeader {

    /**
     * builds basic authentication header from username and password
     *
     * @return base64 encoded header for basic authentication
     */
    public static String buildBasicAuthHeader() throws XrayWrapperException {
        EnvironmentVariableReader environmentVariableReader = new EnvironmentVariableReader();

        String username = environmentVariableReader.readEnvAsString(EnvironmentVariableConstants.XRAY_USERNAME_ENV);
        String password = environmentVariableReader.readEnvAsString(EnvironmentVariableConstants.XRAY_PASSWORD_ENV);
        if (username == null || password == null) {
            throw new XrayWrapperException("Authentication not possible because of missing environment variables XRAY_USER and XRAY_PASSWORD",
                    XrayWrapperExitCode.NOT_NULLABLE);
        }

        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth);
    }
}
