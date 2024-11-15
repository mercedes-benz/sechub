// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.authorization;

import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;

/**
 * This enumeration just represents all possible roles and knows the relation to
 * {@link RoleConstants} values. It is not intended to use for persistence or
 * logic directly.
 *
 * @author Albert Tregnaghi
 *
 */
public enum AuthUserRole {

    USER(RoleConstants.ROLE_USER),

    OWNER(RoleConstants.ROLE_OWNER),

    SUPERADMINISTRATOR(RoleConstants.ROLE_SUPERADMIN);

    private String role;

    private AuthUserRole(String role) {
        this.role = role;
    }

    /**
     * Returns the related role string from {@link RoleConstants}.
     *
     * @return the role string
     */
    public String getRole() {
        return role;
    }

}
