// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.security;

/**
 * SecHub role constants without {@link AuthorityConstants#AUTHORITY_ROLE_PREFIX
 * authority prefix}. Persistence of role data is NOT done with this values, but
 * a boolean values inside AuthUser entity (<i>The transformation is done inside
 * AuthUserUpdateRolesService.</i>)
 *
 * @author Albert Tregnaghi
 *
 */
public class RoleConstants {

    public static final String ROLE_SUPERADMIN = "SUPERADMIN";

    public static final String ROLE_USER = "USER";

    public static final String ROLE_OWNER = "OWNER";

    private RoleConstants() {
    }

    public static boolean isSuperAdminRole(String role) {
        return ROLE_SUPERADMIN.equals(role);
    }

    public static boolean isUserRole(String role) {
        return ROLE_USER.equals(role);
    }

    public static boolean isOwnerRole(String role) {
        return ROLE_OWNER.equals(role);
    }

}
