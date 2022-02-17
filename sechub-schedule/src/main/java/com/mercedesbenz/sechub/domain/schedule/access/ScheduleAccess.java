// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.access;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * This entity represents only the access for users to a project. As long as
 * there are is no tupel (user + project) the access is forbidden or project
 * does not exist. Even an administrator must be added as user. So access is
 * always auditable etc. and we got also no jobs without a correct owner!
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = ScheduleAccess.TABLE_NAME)
public class ScheduleAccess {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "SCHEDULE_ACCESS";

    public static final String COLUMN_PROJECT_ID = "PROJECT_ID";
    public static final String COLUMN_USER_ID = "USER_ID";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = ScheduleAccess.class.getSimpleName();

    @EmbeddedId
    ProjectAccessCompositeKey key;

    @Version
    @Column(name = "VERSION")
    Integer version;

    ScheduleAccess() {
        // jpa only
    }

    public ScheduleAccess(String userId, String projectId) {
        this(new ProjectAccessCompositeKey(userId, projectId));
    }

    public ScheduleAccess(ProjectAccessCompositeKey key) {
        if (key == null) {
            throw new IllegalArgumentException("key may not be null");
        }
        if (key.projectId == null) {
            throw new IllegalArgumentException("key.projectId may not be null");
        }
        this.key = key;
    }

    public ProjectAccessCompositeKey getKey() {
        return key;
    }

    @Embeddable
    public static class ProjectAccessCompositeKey implements Serializable {

        private static final long serialVersionUID = 8753389792382752253L;

        @Column(name = COLUMN_PROJECT_ID, nullable = false)
        private String projectId;

        @Column(name = COLUMN_USER_ID, nullable = false)
        private String userId;

        ProjectAccessCompositeKey() {
            // jpa only
        }

        public ProjectAccessCompositeKey(String userId, String projectId) {
            this.userId = userId;
            this.projectId = projectId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((projectId == null) ? 0 : projectId.hashCode());
            result = prime * result + ((userId == null) ? 0 : userId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ProjectAccessCompositeKey other = (ProjectAccessCompositeKey) obj;
            if (projectId == null) {
                if (other.projectId != null)
                    return false;
            } else if (!projectId.equals(other.projectId))
                return false;
            if (userId == null) {
                if (other.userId != null)
                    return false;
            } else if (!userId.equals(other.userId))
                return false;
            return true;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScheduleAccess other = (ScheduleAccess) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key)) {
            return false;
        }
        return true;
    }

}
