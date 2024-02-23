// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.security;

public enum PDSRoles {

    USER(PDSRoleConstants.ROLE_USER),

    SUPERADMIN(PDSRoleConstants.ROLE_SUPERADMIN);

    private String role;

    private PDSRoles(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
