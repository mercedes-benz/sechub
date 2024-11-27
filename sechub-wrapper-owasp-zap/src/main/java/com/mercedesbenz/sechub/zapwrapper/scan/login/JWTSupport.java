// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan.login;

import java.util.regex.Pattern;

class JWTSupport {

    private static final Pattern JWT_PATTERN = Pattern.compile("^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_.+/=]*$");

    /**
     * Performs some tests to see if the given value is a JWT.
     *
     * @param value a string which could represent a JWT token (look at
     *              https://jwt.io/ for detailed description about JWT content)
     * @return <code>true</code> if all tests pass and the value is a JWT,
     *         <code>false</code> otherwise.
     */
    boolean isJWT(String value) {
        if (value == null) {
            return false;
        }
        if (!JWT_PATTERN.matcher(value).matches()) {
            return false;
        }
        String[] split = value.split("\\.");
        // Simple way to check it is a JWT: When looking at https://jwt.io/ we can see,
        // that every JWT has the structure "eyJ${someData}.eyJ${OtherData}" so we use
        // this to identify JWT. Since this is only used for data extracted from HTTP
        // sessions the test should be sufficient.
        return split[0].startsWith("eyJ") && split[1].startsWith("eyJ");
    }
}
