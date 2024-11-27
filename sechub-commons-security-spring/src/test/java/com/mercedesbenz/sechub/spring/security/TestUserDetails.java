// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Test implementation of {@link UserDetails} for use in Spring Security related
 * tests.
 *
 * <p>
 * Defines an authenticated user that expects a username and a collection of
 * granted authorities in the constructor.
 * </p>
 *
 * <p>
 * For Basic Auth purposes, the password can be set as well. Otherwise it will
 * contain a default value.
 * </p>
 *
 * @author hamidonos
 */
class TestUserDetails implements UserDetails {

    private static final String DEFAULT_PASSWORD = "";
    private static final boolean DEFAULT_IS_ACCOUNT_NON_EXPIRED = true;
    private static final boolean DEFAULT_IS_ACCOUNT_NON_LOCKED = true;
    private static final boolean DEFAULT_IS_CREDENTIALS_NON_EXPIRED = true;
    private static final boolean DEFAULT_IS_ENABLED = true;

    private final Collection<? extends GrantedAuthority> authorities;
    private final String username;
    private final String password;

    TestUserDetails(Collection<? extends GrantedAuthority> authorities, String username) {
        this.authorities = authorities;
        this.username = username;
        this.password = DEFAULT_PASSWORD;
    }

    TestUserDetails(Collection<? extends GrantedAuthority> authorities, String username, String password) {
        this.authorities = authorities;
        this.username = username;
        this.password = password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return DEFAULT_IS_ACCOUNT_NON_EXPIRED;
    }

    @Override
    public boolean isAccountNonLocked() {
        return DEFAULT_IS_ACCOUNT_NON_LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return DEFAULT_IS_CREDENTIALS_NON_EXPIRED;
    }

    @Override
    public boolean isEnabled() {
        return DEFAULT_IS_ENABLED;
    }
}
