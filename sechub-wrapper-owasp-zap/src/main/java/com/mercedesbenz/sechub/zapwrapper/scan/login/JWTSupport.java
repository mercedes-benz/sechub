// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan.login;

import java.util.regex.Pattern;

public class JWTSupport {

    private static final Pattern JWT_PATTERN = Pattern.compile("^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_.+/=]*$");

    /**
     * Performs some tests to see if the given value is a JWT.
     *
     * @param value
     * @return <code>true</code> if all tests pass and the value is a JWT,
     *         <code>false</code> otherwise.
     */
    public boolean isJWT(String value) {
        if (value == null) {
            return false;
        }
        if (!JWT_PATTERN.matcher(value).matches()) {
            return false;
        }
        String[] split = value.split("\\.");
        return split[0].startsWith("eyJ") && split[1].startsWith("eyJ");
    }
}
