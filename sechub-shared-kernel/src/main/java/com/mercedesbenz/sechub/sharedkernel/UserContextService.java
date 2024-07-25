// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

import java.util.Collection;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserContextService {

    /**
     * @return user id of current logged in user or <code>null</code>
     */
    public String getUserId() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return null;
        }
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        }
        return null;
    }

    public boolean isSuperAdmin() {
        return hasRole(RoleConstants.ROLE_SUPERADMIN);
    }

    public String getAuthories() {

        StringBuilder sb = new StringBuilder();
        Authentication authentication = getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority auth : authorities) {
            if (auth == null) {
                continue;
            }
            sb.append(auth.getAuthority());
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    private boolean hasRole(String role) {
        if (role == null) {
            return false;
        }
        String authorityOfRole = AuthorityConstants.AUTHORITY_ROLE_PREFIX + role;

        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority auth : authorities) {
            if (auth == null) {
                continue;
            }
            if (authorityOfRole.equals(auth.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private Authentication getAuthentication() {
        SecurityContext context = getContext();
        if (context == null) {
            return null;
        }
        return getContext().getAuthentication();
    }

    private SecurityContext getContext() {
        return SecurityContextHolder.getContext();
    }

}
