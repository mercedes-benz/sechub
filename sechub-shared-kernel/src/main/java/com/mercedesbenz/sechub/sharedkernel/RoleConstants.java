// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

/**
 * Never change the string content of identifiers! Will be used for persistence
 * and also inside code!
 *
 * @author Albert Tregnaghi
 *
 */
public class RoleConstants {
    /*
     * Never change the string content of identifiers! Will be used for persistence
     * and also inside code!
     */
    public static final String ROLE_SUPERADMIN = "ROLE_SUPERADMIN";

    public static final String ROLE_USER = "ROLE_USER";

    public static final String ROLE_OWNER = "ROLE_OWNER";

    private RoleConstants() {
    }

    public static final boolean isSuperAdminRole(String role) {
        return ROLE_SUPERADMIN.equals(role);
    }

    public static final boolean isUserRole(String role) {
        return ROLE_USER.equals(role);
    }

    public static final boolean isOwnerRole(String role) {
        return ROLE_OWNER.equals(role);
    }

}
