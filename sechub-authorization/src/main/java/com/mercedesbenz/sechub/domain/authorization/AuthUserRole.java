// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.authorization;

import java.util.List;

import com.mercedesbenz.sechub.sharedkernel.RoleConstants;

public enum AuthUserRole {

    USER(RoleConstants.ROLE_USER),

    OWNER(RoleConstants.ROLE_OWNER),

    SUPERADMINISTRATOR(RoleConstants.ROLE_SUPERADMIN);

    private String id;

    private AuthUserRole(String id) {
        // Identifier used for persistence - do NEVER change an existing id this when
        // you not want to
        // migrate your database!
        // we use this as identifier in db to prevent side effects on refactorings
        // So developers are able to change the enum naming without side effects on
        // db...
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static AuthUserRole fromId(String id) {
        for (AuthUserRole r : AuthUserRole.values()) {
            if (r.id.equals(id)) {
                return r;
            }
        }
        return null;
    }

    public boolean isRepresentedByOneof(List<String> roleIds) {
        return isContaining(AuthUserRole.SUPERADMINISTRATOR, roleIds);
    }

    private static boolean isContaining(AuthUserRole search, List<String> givenIds) {
        if (givenIds == null || givenIds.isEmpty()) {
            return false;
        }
        for (String roleId : givenIds) {
            AuthUserRole foundOrNull = AuthUserRole.fromId(roleId);
            if (search.equals(foundOrNull)) {
                return true;
            }
        }
        return false;
    }
}
