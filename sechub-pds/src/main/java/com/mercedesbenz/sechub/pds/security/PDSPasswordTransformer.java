// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.security;

/**
 * Forcibly disable encoding of passwords from profiles with help of {noop}
 * prefix. Ignore passwords with {bcrypt} prefix.
 */
public class PDSPasswordTransformer {

    public String transformPassword(String originPassword) {
        if (originPassword == null) {
            throw new IllegalArgumentException("Password may not be null!");
        }

        if (originPassword.startsWith("{noop}") || originPassword.startsWith("{bcrypt}"))
            return originPassword;

        return "{noop}" + originPassword;
    }
}