// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.authorization;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

class AuthUserDetailsTest {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final boolean ENABLED = true;
    private static final boolean ACCOUNT_NON_EXPIRED = true;
    private static final boolean CREDENTIALS_NON_EXPIRED = true;
    private static final boolean ACCOUNT_NON_LOCKED = true;
    private static final Collection<? extends GrantedAuthority> AUTHORITIES = List.of(() -> "ROLE_USER");

    @Test
    void construct_auth_user_details() {
        /* execute */
        AuthUserDetails result = new AuthUserDetails(USERNAME, PASSWORD, ENABLED, ACCOUNT_NON_EXPIRED, CREDENTIALS_NON_EXPIRED, ACCOUNT_NON_LOCKED, AUTHORITIES);

        /* test */
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(USERNAME);
        assertThat(result.getPassword()).isEqualTo(PASSWORD);
        assertThat(result.isEnabled()).isEqualTo(ENABLED);
        assertThat(result.isAccountNonExpired()).isEqualTo(ACCOUNT_NON_EXPIRED);
        assertThat(result.isCredentialsNonExpired()).isEqualTo(CREDENTIALS_NON_EXPIRED);
        assertThat(result.isAccountNonLocked()).isEqualTo(ACCOUNT_NON_LOCKED);
        assertThat(result.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_USER");
    }

    @Test
    void build_auth_user_details() {
        /* prepare */
        AuthUserDetails.Builder builder = new AuthUserDetails.Builder();
        builder.username(USERNAME);
        builder.password(PASSWORD);
        builder.enabled(ENABLED);
        builder.accountNonExpired(ACCOUNT_NON_EXPIRED);
        builder.credentialsNonExpired(CREDENTIALS_NON_EXPIRED);
        builder.accountNonLocked(ACCOUNT_NON_LOCKED);
        builder.authorities(AUTHORITIES);

        /* execute */
        AuthUserDetails result = builder.build();

        /* test */
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(USERNAME);
        assertThat(result.getPassword()).isEqualTo(PASSWORD);
        assertThat(result.isEnabled()).isEqualTo(ENABLED);
        assertThat(result.isAccountNonExpired()).isEqualTo(ACCOUNT_NON_EXPIRED);
        assertThat(result.isCredentialsNonExpired()).isEqualTo(CREDENTIALS_NON_EXPIRED);
        assertThat(result.isAccountNonLocked()).isEqualTo(ACCOUNT_NON_LOCKED);
        assertThat(result.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_USER");
    }

}
