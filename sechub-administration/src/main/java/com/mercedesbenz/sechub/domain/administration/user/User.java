// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

@Entity
@Table(name = User.TABLE_NAME)
/**
 * Represents a SecHub user. Project associations to project only exists in
 * database but not inside the ORM entity (to increase ORM/DB performance). All
 * project related associations are handled by Project object,repository and
 * services!
 */
public class User {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "ADM_USER";
    public static final String TABLE_NAME_PROJECT_TO_USER = "ADM_PROJECT_TO_USER";

    public static final String COLUMN_USER_ID = "USER_ID";
    public static final String COLUMN_USER_HASHED_API_TOKEN = "USER_APITOKEN";
    /**
     * A one time token represents a special token which can be used just one time
     */
    public static final String COLUMN_USER_ONE_TIME_TOKEN = "USER_ONETIMETOKEN";
    public static final String COLUMN_USER_ONE_TIME_TOKEN_CREATED = "USER_OTT_CREATED";
    public static final String COLUMN_USER_ENABLED = "USER_ENABLED";

    public static final String COLUMN_EMAIL_ADDRESS = "USER_EMAIL_ADDRESS";
    public static final String COLUMN_USER_ROLES = "USER_ROLES";
    public static final String COLUMN_USER_SUPERADMIN = "USER_SUPERADMIN";
    public static final String COLUMN_USER_DEACTIVATED = "USER_DEACTIVATED";
    public static final String COLUMN_USER_PROJECTS = "PROJECTS_PROJECT_ID";

    public static final String ASSOCIATE_PROJECT_TO_USER_COLUMN_USER_ID = "USERS_USER_ID";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = User.class.getSimpleName();

    public static final String PROPERTY_USER_NAME = "name";
    public static final String PROPERTY_USER_EMAILADDRESS = "emailAddress";

    @Id
    @Column(name = COLUMN_USER_ID, unique = true, nullable = false)
    String name;

    @Column(name = COLUMN_EMAIL_ADDRESS, unique = true, nullable = false)
    String emailAddress;

    @Column(name = COLUMN_USER_HASHED_API_TOKEN, nullable = false)
    String hashedApiToken;

    @Column(name = COLUMN_USER_ONE_TIME_TOKEN, nullable = true)
    String oneTimeToken;

    @Column(name = COLUMN_USER_ONE_TIME_TOKEN_CREATED, nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    Date oneTimeTokenDate;

    @Column(name = COLUMN_USER_SUPERADMIN)
    boolean superAdmin;

    @Column(name = COLUMN_USER_DEACTIVATED)
    boolean deactivated;

    @Version
    @Column(name = "VERSION")
    Integer version;

    public boolean isSuperAdmin() {
        return superAdmin;
    }

    public boolean isDeactivated() {
        return deactivated;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getName() {
        return name;
    }

    public String getHashedApiToken() {
        return hashedApiToken;
    }

    public String getOneTimeToken() {
        return oneTimeToken;
    }

    public Date getOneTimeTokenDate() {
        return oneTimeTokenDate;
    }

    /**
     * Returns <code>true</code> when one time token is outdated or not existing
     *
     * @param timeOutMillis
     * @return <code>true</code> when one time token is outdated
     */
    public boolean isOneTimeTokenOutDated(long timeOutMillis) {
        if (oneTimeTokenDate == null) {
            return true;
        }
        if (oneTimeToken == null || oneTimeToken.isEmpty()) {
            return true;
        }
        long current = System.currentTimeMillis();
        long diff = current - oneTimeTokenDate.getTime();
        return diff > timeOutMillis;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
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
        User other = (User) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

}