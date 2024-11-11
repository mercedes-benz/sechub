// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.authorization;

import static com.mercedesbenz.sechub.sharedkernel.security.AuthorityConstants.*;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;

public class AuthUserDetailsServiceTest {

    @Test
    public void adoptUserAccumultesAuthorities() {
        AuthUser entity = createAuthUser();

        entity.setRoleOwner(true);
        entity.setRoleUser(true);
        entity.setRoleSuperAdmin(true);

        /* execute */
        UserDetails result = AuthUserDetailsService.adoptUser(entity);

        /* test */
        assertHasAuthority(result, AUTHORITY_ROLE_PREFIX + RoleConstants.ROLE_USER, AUTHORITY_ROLE_PREFIX + RoleConstants.ROLE_SUPERADMIN,
                AUTHORITY_ROLE_PREFIX + RoleConstants.ROLE_OWNER);
    }

    @Test
    public void adoptUser_with_role_user() {
        AuthUser entity = createAuthUser();

        entity.setRoleOwner(false);
        entity.setRoleUser(true);
        entity.setRoleSuperAdmin(false);

        /* execute */
        UserDetails result = AuthUserDetailsService.adoptUser(entity);

        /* test */
        assertHasAuthority(result, AUTHORITY_ROLE_PREFIX + RoleConstants.ROLE_USER);
    }

    @Test
    public void adoptUser_with_role_owner() {
        /* prepare */
        AuthUser entity = createAuthUser();

        entity.setRoleOwner(true);
        entity.setRoleUser(false);
        entity.setRoleSuperAdmin(false);

        /* execute */
        UserDetails result = AuthUserDetailsService.adoptUser(entity);

        /* test */
        assertHasAuthority(result, AUTHORITY_ROLE_PREFIX + RoleConstants.ROLE_OWNER);
    }

    @Test
    public void adoptUser_with_role_superadmin() {
        AuthUser entity = createAuthUser();

        entity.setRoleOwner(false);
        entity.setRoleUser(false);
        entity.setRoleSuperAdmin(true);

        /* execute */
        UserDetails result = AuthUserDetailsService.adoptUser(entity);

        /* test */
        assertHasAuthority(result, AUTHORITY_ROLE_PREFIX + RoleConstants.ROLE_SUPERADMIN);
    }

    private void assertHasAuthority(UserDetails result, String... authorities) {
        Collection<? extends GrantedAuthority> auth = result.getAuthorities();
        for (String authority : authorities) {
            boolean found = false;
            for (Iterator<? extends GrantedAuthority> it = auth.iterator(); it.hasNext();) {
                GrantedAuthority ga = it.next();
                if (authority.contentEquals(ga.getAuthority())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail("Did not found role:" + authority + " inside " + auth);
            }

        }
        assertEquals(authorities.length, auth.size());

    }

    private AuthUser createAuthUser() {
        AuthUser entity = new AuthUser();

        entity.setUserId("albert");
        entity.setHashedApiToken("{noop}top-secret");
        return entity;
    }

}
