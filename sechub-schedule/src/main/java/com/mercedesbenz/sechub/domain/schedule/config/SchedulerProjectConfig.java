// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.config;

import java.util.Objects;

import com.mercedesbenz.sechub.sharedkernel.project.ProjectAccessLevel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Configuration entry for scheduler project configuration data inside database.
 * Here we store some project specific data.
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = SchedulerProjectConfig.TABLE_NAME)
public class SchedulerProjectConfig {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "SCHEDULE_PROJECT_CONFIG";

    public static final String COLUMN_PROJECT_ID = "PROJECT_ID";
    public static final String COLUMN_PROJECT_ACCESS_LEVEL = "PROJECT_ACCESS_LEVEL";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = SchedulerProjectConfig.class.getSimpleName();

    @Id
    @Column(name = COLUMN_PROJECT_ID, updatable = false, nullable = false)
    String projectId;

    @Column(name = COLUMN_PROJECT_ACCESS_LEVEL, nullable = false)
    ProjectAccessLevel projectAccessLevel = ProjectAccessLevel.FULL; // per default full access

    @Version
    @Column(name = "VERSION")
    Integer version;

    public void setProjectAccessLevel(ProjectAccessLevel projectAccessLevel) {
        this.projectAccessLevel = projectAccessLevel;
    }

    public ProjectAccessLevel getProjectAccessLevel() {
        return projectAccessLevel;
    }

    public String getProjectId() {
        return projectId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((projectId == null) ? 0 : projectId.hashCode());
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
        SchedulerProjectConfig other = (SchedulerProjectConfig) obj;
        return Objects.equals(projectId, other.projectId);
    }

}