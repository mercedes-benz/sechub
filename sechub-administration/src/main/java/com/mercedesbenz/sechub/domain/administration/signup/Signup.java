// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.signup;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = Signup.TABLE_NAME)
public class Signup {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "ADM_USER_SELFREGISTRATION";

    /**
     * Email adress is also the primary key. So no duplicates
     */
    public static final String COLUMN_EMAIL_ADRESS = "EMAIL_ADRESS";
    public static final String COLUMN_USER_ID = "USER_ID";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = Signup.class.getSimpleName();

    @Id
    @Column(name = COLUMN_USER_ID)
    String userId;

    @Column(name = COLUMN_EMAIL_ADRESS, nullable = false)
    String emailAdress;

    @Version
    @Column(name = "VERSION")
    Integer version;

    public void setEmailAdress(String emailAdress) {
        this.emailAdress = emailAdress;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmailAdress() {
        return emailAdress;
    }

    public String getUserId() {
        return userId;
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
        Signup other = (Signup) obj;
        return Objects.equals(userId, other.userId);
    }

}