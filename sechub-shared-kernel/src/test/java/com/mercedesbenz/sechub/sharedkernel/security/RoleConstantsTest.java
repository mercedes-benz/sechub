// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.security;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class RoleConstantsTest {

    @Test
    void isSuperAdminRole_works() {
        assertTrue(RoleConstants.isSuperAdminRole("SUPERADMIN"));
        assertTrue(RoleConstants.isSuperAdminRole(RoleConstants.ROLE_SUPERADMIN));

        assertFalse(RoleConstants.isSuperAdminRole("USER"));
        assertFalse(RoleConstants.isSuperAdminRole(RoleConstants.ROLE_USER));
        assertFalse(RoleConstants.isSuperAdminRole(RoleConstants.ROLE_OWNER));
        assertFalse(RoleConstants.isSuperAdminRole(""));
        assertFalse(RoleConstants.isSuperAdminRole("X"));
    }

    @Test
    void isUserRole_works() {
        assertTrue(RoleConstants.isUserRole("USER"));
        assertTrue(RoleConstants.isUserRole(RoleConstants.ROLE_USER));

        assertFalse(RoleConstants.isUserRole("SUPERADMIN"));
        assertFalse(RoleConstants.isUserRole(RoleConstants.ROLE_SUPERADMIN));
        assertFalse(RoleConstants.isUserRole(""));
        assertFalse(RoleConstants.isUserRole("X"));
    }

    @Test
    void getAllRoles_contains_expected_roles() {
        assertThat(RoleConstants.getAllRoles()).containsOnly(RoleConstants.ROLE_OWNER, RoleConstants.ROLE_SUPERADMIN, RoleConstants.ROLE_USER)
                .doesNotHaveDuplicates();
    }

}
