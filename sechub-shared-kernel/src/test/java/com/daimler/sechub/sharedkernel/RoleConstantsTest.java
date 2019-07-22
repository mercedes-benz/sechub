// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel;

import static org.junit.Assert.*;

import org.junit.Test;

public class RoleConstantsTest {

	@Test
	public void isSuperAdminRole_works() {
		assertTrue(RoleConstants.isSuperAdminRole("ROLE_SUPERADMIN"));
		
		assertFalse(RoleConstants.isSuperAdminRole("ROLE_USER"));
		assertFalse(RoleConstants.isSuperAdminRole(""));
		assertFalse(RoleConstants.isSuperAdminRole("X"));
	}
	
	@Test
	public void isUserRole_works() {
		assertTrue(RoleConstants.isUserRole("ROLE_USER"));

		assertFalse(RoleConstants.isUserRole("ROLE_SUPERADMIN"));
		assertFalse(RoleConstants.isUserRole(""));
		assertFalse(RoleConstants.isUserRole("X"));
	}

}
