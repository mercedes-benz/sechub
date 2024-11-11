// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.authorization.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.mercedesbenz.sechub.domain.authorization.AuthUser;
import com.mercedesbenz.sechub.domain.authorization.AuthUserRepository;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

public class AuthUserUpdateRolesServiceTest {

    private AuthUserUpdateRolesService serviceToTest;
    private AuthUserRepository authUserRepository;

    @Before
    public void before() throws Exception {
        authUserRepository = mock(AuthUserRepository.class);

        serviceToTest = new AuthUserUpdateRolesService();
        serviceToTest.authUserRepository = authUserRepository;
        serviceToTest.assertion = mock(UserInputAssertion.class);
    }

    @Test
    public void a_user_having_no_role_and_updated_as_user_has_role_user() {
        /* prepare */
        AuthUser user = new AuthUser();
        when(authUserRepository.findOrFail("user")).thenReturn(user);

        /* execute */
        serviceToTest.updateRoles("user", Collections.singleton(RoleConstants.ROLE_USER));

        /* test */
        assertTrue(user.isRoleUser());
        assertFalse(user.isRoleOwner());
        assertFalse(user.isRoleSuperAdmin());
    }

    @Test
    public void a_user_having_no_role_and_updated_as_superadmin_has_role_superadmin() {
        /* prepare */
        AuthUser user = new AuthUser();
        when(authUserRepository.findOrFail("user")).thenReturn(user);

        /* execute */
        serviceToTest.updateRoles("user", Collections.singleton(RoleConstants.ROLE_SUPERADMIN));

        /* test */
        assertFalse(user.isRoleUser());
        assertFalse(user.isRoleOwner());
        assertTrue(user.isRoleSuperAdmin());
    }

    @Test
    public void a_user_having_no_role_and_updated_as_owner_has_role_owner() {
        /* prepare */
        AuthUser user = new AuthUser();
        when(authUserRepository.findOrFail("user")).thenReturn(user);

        /* execute */
        serviceToTest.updateRoles("user", Collections.singleton(RoleConstants.ROLE_OWNER));

        /* test */
        assertFalse(user.isRoleUser());
        assertTrue(user.isRoleOwner());
        assertFalse(user.isRoleSuperAdmin());
    }

    @Test
    public void a_user_having_no_role_and_updated_as_user_and_super_admin_has_role_user_and_superadmin() {
        /* prepare */
        AuthUser user = new AuthUser();
        when(authUserRepository.findOrFail("user")).thenReturn(user);

        /* execute */
        serviceToTest.updateRoles("user", new LinkedHashSet<>(Arrays.asList(RoleConstants.ROLE_SUPERADMIN, RoleConstants.ROLE_USER)));

        /* test */
        assertTrue(user.isRoleUser());
        assertFalse(user.isRoleOwner());
        assertTrue(user.isRoleSuperAdmin());
    }

    @Test
    public void a_user_having_no_role_and_updated_as_user_and_owner__has_role_owner_and_user() {
        /* prepare */
        AuthUser user = new AuthUser();
        when(authUserRepository.findOrFail("user")).thenReturn(user);

        /* execute */
        serviceToTest.updateRoles("user", new LinkedHashSet<>(Arrays.asList(RoleConstants.ROLE_OWNER, RoleConstants.ROLE_USER)));

        /* test */
        assertTrue(user.isRoleUser());
        assertTrue(user.isRoleOwner());
        assertFalse(user.isRoleSuperAdmin());
    }

    @Test
    public void a_user_having_role_user_and_updated_as_owner__has_role_owner_and_no_longer_user() {
        /* prepare */
        AuthUser user = new AuthUser();
        user.setRoleUser(true);
        when(authUserRepository.findOrFail("user")).thenReturn(user);

        /* execute */
        serviceToTest.updateRoles("user", new LinkedHashSet<>(Arrays.asList(RoleConstants.ROLE_OWNER)));

        /* test */
        assertFalse(user.isRoleUser());
        assertTrue(user.isRoleOwner());
        assertFalse(user.isRoleSuperAdmin());
    }

    @Test
    public void a_user_having_role_superadmin_and_updated_as_user_only__has_role_user_and_no_longer_superadmin() {
        /* prepare */
        AuthUser user = new AuthUser();
        user.setRoleSuperAdmin(true);
        when(authUserRepository.findOrFail("user")).thenReturn(user);

        /* execute */
        serviceToTest.updateRoles("user", new LinkedHashSet<>(Arrays.asList(RoleConstants.ROLE_USER)));

        /* test */
        assertTrue(user.isRoleUser());
        assertFalse(user.isRoleOwner());
        assertFalse(user.isRoleSuperAdmin());
    }

    @Test
    public void a_user_having_role_superadmin_and_updated_with_empty_list_has_no_roles_at_all() {
        /* prepare */
        AuthUser user = new AuthUser();
        user.setRoleSuperAdmin(true);
        when(authUserRepository.findOrFail("user")).thenReturn(user);

        /* execute */
        serviceToTest.updateRoles("user", new LinkedHashSet<>());

        /* test */
        assertFalse(user.isRoleUser());
        assertFalse(user.isRoleOwner());
        assertFalse(user.isRoleSuperAdmin());
    }

    @Test
    public void when_update_service_is_called_the_user_entity_is_persisted_after_changed() {
        /* prepare */
        AuthUser user = mock(AuthUser.class);
        when(authUserRepository.findOrFail("user")).thenReturn(user);

        /* execute */
        serviceToTest.updateRoles("user", new LinkedHashSet<>(Arrays.asList(RoleConstants.ROLE_USER)));

        /* test */
        InOrder inOrder = inOrder(authUserRepository, user, user, user, user, authUserRepository);
        // first get it
        inOrder.verify(authUserRepository).findOrFail("user");
        // reset flags
        inOrder.verify(user).setRoleSuperAdmin(false);
        inOrder.verify(user).setRoleOwner(false);
        inOrder.verify(user).setRoleUser(false);

        // set as role owner
        inOrder.verify(user).setRoleUser(true);

        // after this save the changed entity
        inOrder.verify(authUserRepository).save(user);
    }

}
