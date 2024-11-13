// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.security;

import static org.junit.Assert.*;

import org.junit.Test;

public class RoleConstantsTest {

    @Test
    public void isSuperAdminRole_works() {
        assertTrue(RoleConstants.isSuperAdminRole("SUPERADMIN"));
        assertTrue(RoleConstants.isSuperAdminRole(RoleConstants.ROLE_SUPERADMIN));

        assertFalse(RoleConstants.isSuperAdminRole("USER"));
        assertFalse(RoleConstants.isSuperAdminRole(RoleConstants.ROLE_USER));
        assertFalse(RoleConstants.isSuperAdminRole(RoleConstants.ROLE_OWNER));
        assertFalse(RoleConstants.isSuperAdminRole(""));
        assertFalse(RoleConstants.isSuperAdminRole("X"));
    }

    @Test
    public void isUserRole_works() {
        assertTrue(RoleConstants.isUserRole("USER"));
        assertTrue(RoleConstants.isUserRole(RoleConstants.ROLE_USER));

        assertFalse(RoleConstants.isUserRole("SUPERADMIN"));
        assertFalse(RoleConstants.isUserRole(RoleConstants.ROLE_SUPERADMIN));
        assertFalse(RoleConstants.isUserRole(""));
        assertFalse(RoleConstants.isUserRole("X"));
    }

}
