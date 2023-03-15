// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.authorization;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mercedesbenz.sechub.sharedkernel.RoleConstants;

public class AuthUserRestAPIConfigurationTest {

    @Test
    public void adoptUserAcumultesAuthorities() {
        AuthUser entity = createAuthUser();

        entity.setRoleOwner(true);
        entity.setRoleUser(true);
        entity.setRoleSuperAdmin(true);

        /* execute */
        UserDetails result = AuthUserRestAPIConfiguration.adoptUser(entity);

        /* test */
        assertHasAuthority(result, RoleConstants.ROLE_USER, RoleConstants.ROLE_SUPERADMIN, RoleConstants.ROLE_OWNER);
    }

    @Test
    public void adoptUser_with_role_user() {
        AuthUser entity = createAuthUser();

        entity.setRoleOwner(false);
        entity.setRoleUser(true);
        entity.setRoleSuperAdmin(false);

        /* execute */
        UserDetails result = AuthUserRestAPIConfiguration.adoptUser(entity);

        /* test */
        assertHasAuthority(result, RoleConstants.ROLE_USER);
    }

    @Test
    public void adoptUser_with_role_owner() {
        /* prepare */
        AuthUser entity = createAuthUser();

        entity.setRoleOwner(true);
        entity.setRoleUser(false);
        entity.setRoleSuperAdmin(false);

        /* execute */
        UserDetails result = AuthUserRestAPIConfiguration.adoptUser(entity);

        /* test */
        assertHasAuthority(result, RoleConstants.ROLE_OWNER);
    }

    @Test
    public void adoptUser_with_role_superadmin() {
        AuthUser entity = createAuthUser();

        entity.setRoleOwner(false);
        entity.setRoleUser(false);
        entity.setRoleSuperAdmin(true);

        /* execute */
        UserDetails result = AuthUserRestAPIConfiguration.adoptUser(entity);

        /* test */
        assertHasAuthority(result, RoleConstants.ROLE_SUPERADMIN);
    }

    private void assertHasAuthority(UserDetails result, String... roles) {
        Collection<? extends GrantedAuthority> auth = result.getAuthorities();
        for (String role : roles) {
            boolean found = false;
            for (Iterator<? extends GrantedAuthority> it = auth.iterator(); it.hasNext();) {
                GrantedAuthority ga = it.next();
                if (role.contentEquals(ga.getAuthority())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail("Did not found role:" + role + " inside " + auth);
            }

        }
        assertEquals(roles.length, auth.size());

    }

    private AuthUser createAuthUser() {
        AuthUser entity = new AuthUser();

        entity.setUserId("albert");
        entity.setHashedApiToken("{noop}top-secret");
        return entity;
    }

}
