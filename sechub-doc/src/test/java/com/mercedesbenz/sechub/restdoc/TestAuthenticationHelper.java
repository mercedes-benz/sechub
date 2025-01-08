// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import java.util.Base64;

public class TestAuthenticationHelper {
    public static final String HEADER_NAME = "Authorization";
    public static final String HEADER_DESCRIPTION = "Basic authentication credentials";
    private static final String USER_TOKEN = "user:secret";

    public static String getHeaderValue() {
        String userAndTokenEncoded = Base64.getEncoder().encodeToString(USER_TOKEN.getBytes());

        return "Basic " + userAndTokenEncoded;
    }
}
