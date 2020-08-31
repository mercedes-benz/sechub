// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.security;

public enum PDSRoles {
    USER(PDSRoleConstants.ROLE_USER),
    
    SUPERADMIN(PDSRoleConstants.ROLE_SUPERADMIN);
    
    private String id;

    private PDSRoles(String id) {
        this.id=id;
    }
    
    public String getRoleId() {
        return id;
    }
    
    public String getRole() {
        return name();
    }
}
