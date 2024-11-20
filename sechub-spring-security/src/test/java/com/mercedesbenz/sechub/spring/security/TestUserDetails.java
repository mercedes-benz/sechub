// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

class TestUserDetails implements UserDetails {

    private final Collection<? extends GrantedAuthority> authorities;
    private final String username;

    TestUserDetails(Collection<? extends GrantedAuthority> authorities, String username) {
        this.authorities = authorities;
        this.username = username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
