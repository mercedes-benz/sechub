// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import java.util.Objects;

/**
 * Represents reduced information about users for a project
 */
public class ProjectUserData implements Comparable<ProjectUserData> {

    public static final String PROPERTY_USER_ID = "userId";
    public static final String PROPERTY_EMAIL_ADDRESS = "emailAddress";

    public static final String FULL_CLASSNAME = "com.mercedesbenz.sechub.domain.administration.project.ProjectUserData";

    private String userId;
    private String emailAddress;

    public ProjectUserData() {

    }

    /**
     * Used by Spring query to fetch user data directly as DTO - keep this
     * constructor!
     *
     * @param userId
     * @param emailAddress
     */
    public ProjectUserData(String userId, String emailAddress) {
        this.userId = userId;
        this.emailAddress = emailAddress;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Override
    public int hashCode() {
        return Objects.hash(emailAddress, userId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProjectUserData other = (ProjectUserData) obj;
        return Objects.equals(userId, other.userId);
    }

    @Override
    public int compareTo(ProjectUserData o) {
        if (userId == null) {
            if (o.userId == null) {
                return 0;
            }
            return -1;
        }
        if (o == null || o.userId == null) {
            return 1;
        }

        return userId.compareTo(o.userId);
    }

    @Override
    public String toString() {
        return "ProjectUserData [" + (userId != null ? "userId=" + userId + ", " : "") + (emailAddress != null ? "emailAddress=" + emailAddress : "") + "]";
    }
}
