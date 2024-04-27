// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.security;

import java.util.Collection;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PDSUserContextService {

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
        return hasRole(PDSRoleConstants.ROLE_SUPERADMIN);
    }

    private boolean hasRole(String role) {
        if (role == null) {
            return false;
        }
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return false;
        }
        String authorityForRole = PDSAuthorityConstants.AUTHORITY_ROLE_PREFIX + role;
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority auth : authorities) {
            if (auth == null) {
                continue;
            }
            if (authorityForRole.equals(auth.getAuthority())) {
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
