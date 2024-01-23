// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.authorization;

import java.util.Objects;

import com.mercedesbenz.sechub.sharedkernel.RoleConstants;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * This entity is designed to have a entry which is collected as fast as
 * possible from database. So it shall have only rudimentary data necessary to
 * get the info into security to identify access and roles. So we do e.g NOT use
 * lists for {@link AuthUserRole} or {@link RoleConstants} here but use
 * dedicated boolean flags only.
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = AuthUser.TABLE_NAME)
public class AuthUser {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "AUTH_USER";
    public static final String TABLE_ROLES_NAME = "AUTH_USER2ROLES";

    public static final String COLUMN_USER_ID = "USER_ID";
    public static final String COLUMN_USER_API_TOKEN = "USER_APITOKEN";
    public static final String COLUMN_ROLE_USER = "ROLE_USER";
    public static final String COLUMN_ROLE_OWNER = "ROLE_OWNER";
    public static final String COLUMN_ROLE_SUPERADMIN = "ROLE_ADMIN";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = AuthUser.class.getSimpleName();
    public static final String QUERY_COUNT_SUPERADMINS = "SELECT count(u) FROM AuthUser u WHERE u.roleSuperAdmin = true";

    public static final String PROPERTY_USER = "roleUser";
    public static final String PROPERTY_OWNER = "roleOwner";
    public static final String PROPERTY_SUPERADMIN = "roleSuperAdmin";

    @Id
    @Column(name = COLUMN_USER_ID) // ,unique = true, nullable = false)
    String userId;

    @Column(name = COLUMN_USER_API_TOKEN, nullable = true)
    String hashedApiToken;

    @Column(name = COLUMN_ROLE_USER)
    boolean roleUser;

    /**
     * This field is referenced by {@link #PROPERTY_SUPERADMIN}
     */
    @Column(name = COLUMN_ROLE_SUPERADMIN)
    boolean roleSuperAdmin;

    @Column(name = COLUMN_ROLE_OWNER)
    boolean roleOwner;

    @Version
    @Column(name = "VERSION")
    Integer version;

    public boolean isRoleSuperAdmin() {
        return roleSuperAdmin;
    }

    public void setRoleSuperAdmin(boolean roleSuperAdmin) {
        this.roleSuperAdmin = roleSuperAdmin;
    }

    public void setRoleUser(boolean roleUser) {
        this.roleUser = roleUser;
    }

    public void setRoleOwner(boolean roleOwner) {
        this.roleOwner = roleOwner;
    }

    public void setHashedApiToken(String hashedApiToken) {
        this.hashedApiToken = hashedApiToken;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isRoleUser() {
        return roleUser;
    }

    public boolean isRoleOwner() {
        return roleOwner;
    }

    public String getUserId() {
        return userId;
    }

    public String getHashedApiToken() {
        return hashedApiToken;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AuthUser other = (AuthUser) obj;
        return Objects.equals(userId, other.userId);
    }

    @Override
    public String toString() {
        return "AuthUser [userId=" + userId + ", roleUser=" + roleUser + ", roleSuperAdmin=" + roleSuperAdmin + ", roleOwner=" + roleOwner + ", hashedApiToken="
                + hashedApiToken + ", version=" + version + "]";
    }

}