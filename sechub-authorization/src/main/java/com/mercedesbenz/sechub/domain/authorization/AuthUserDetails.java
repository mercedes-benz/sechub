package com.mercedesbenz.sechub.domain.authorization;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import javax.crypto.SealedObject;
import java.util.Collection;

/**
 * Represents the authenticated user in the SecHub application.
 *
 * <p>
 *     Extends the Spring Security {@link User} class to retain the users' password which is helpful in later stages of
 *     application. This is done because Spring Security removes the password from the user object after authentication.
 * </p>
 *
 * @author hamidonos
 */
public class AuthUserDetails extends User {
    public static final AuthUserDetails.Builder BUILDER = new AuthUserDetails.Builder();

    /**
     * The sealed password of the user. Doing this prevents the password from being leaked in any logs or dumps.
     */
    private final SealedObject passwordSealed;

    /* @formatter:off */
    public AuthUserDetails(String username,
                           String password,
                           boolean enabled,
                           boolean accountNonExpired,
                           boolean credentialsNonExpired,
                           boolean accountNonLocked,
                           Collection<? extends GrantedAuthority> authorities) {
        /* @formatter:on */
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.passwordSealed = CryptoAccess.CRYPTO_STRING.seal(password);
    }

    /**
     * This will override the {@link org.springframework.security.core.userdetails.UserDetails#getPassword()} method
     * making the object return the users password instead of <code>null</code>.
     */
    @Override
    public String getPassword() {
        return CryptoAccess.CRYPTO_STRING.unseal(passwordSealed);
    }

    public static class Builder {
        private String username;
        private String password;
        private boolean enabled;
        private boolean accountNonExpired;
        private boolean credentialsNonExpired;
        private boolean accountNonLocked;
        private Collection<? extends GrantedAuthority> authorities;

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder accountNonExpired(boolean accountNonExpired) {
            this.accountNonExpired = accountNonExpired;
            return this;
        }

        public Builder credentialsNonExpired(boolean credentialsNonExpired) {
            this.credentialsNonExpired = credentialsNonExpired;
            return this;
        }

        public Builder accountNonLocked(boolean accountNonLocked) {
            this.accountNonLocked = accountNonLocked;
            return this;
        }

        public Builder authorities(Collection<? extends GrantedAuthority> authorities) {
            this.authorities = authorities;
            return this;
        }

        public AuthUserDetails build() {
            return new AuthUserDetails(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        }
    }
}
