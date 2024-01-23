// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.security;

/**
 * PDS role constants without authority prefix "ROLE_"! Remark: Content is only
 * used at runtime and not persisted.
 *
 * @author Albert Tregnaghi
 *
 */
public class PDSRoleConstants {

    public static final String ROLE_SUPERADMIN = "SUPERADMIN";

    public static final String ROLE_USER = "USER";

    private PDSRoleConstants() {
    }

    public static final boolean isSuperAdminRole(String role) {
        return ROLE_SUPERADMIN.equals(role);
    }

    public static final boolean isUserRole(String role) {
        return ROLE_USER.equals(role);
    }

}
