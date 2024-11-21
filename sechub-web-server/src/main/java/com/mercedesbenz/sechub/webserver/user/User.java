// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.user;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = User.TABLE_NAME)
public class User {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "ADM_USER";
    public static final String COLUMN_USER_ID = "USER_ID";
    public static final String COLUMN_EMAIL_ADDRESS = "USER_EMAIL_ADDRESS";
    public static final String COLUMN_USER_ROLES = "USER_ROLES";


    @Id
    @Column(name = COLUMN_USER_ID, unique = true, nullable = false)
    private String name;

    @Column(name = COLUMN_EMAIL_ADDRESS, unique = true, nullable = false)
    private String emailAddress;

    @Column(name = COLUMN_USER_ROLES, nullable = false)
    private String roles;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }
}