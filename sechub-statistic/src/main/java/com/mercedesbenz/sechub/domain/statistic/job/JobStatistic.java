// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.statistic.job;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Represents statistic data for a job - contains only parts which will not
 * change on job runs. See
 * /sechub-doc/src/docs/asciidoc/diagrams/diagram_em_statistic.puml for details
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = JobStatistic.TABLE_NAME)
@JsonIgnoreProperties(value = { "version" }, ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class JobStatistic {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "STATISTIC_JOB";

    public static final String COLUMN_SECHUB_JOB_UUID = "SECHUB_JOB_UUID";

    public static final String COLUMN_CREATED = "CREATED";
    public static final String COLUMN_PROJECT_ID = "PROJECT_ID";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = JobStatistic.class.getSimpleName();

    public static final String PROPERTY_SECHUB_JOB_UUID = "sechubJobUUID";
    public static final String PROPERTY_CREATED = "created";
    public static final String PROPERTY_PROJECT_ID = "projectId";

    @Id
    @Column(name = COLUMN_SECHUB_JOB_UUID, nullable = false, columnDefinition = "UUID")
    UUID sechubJobUUID;

    @Column(name = COLUMN_PROJECT_ID, nullable = false)
    String projectId;

    @Version
    @Column(name = "VERSION")
    Integer version;

    @Column(name = COLUMN_CREATED) // remark: we setup hibernate to use UTC settings - see application.properties
    LocalDateTime created;

    public JobStatistic() {
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setSechubJobUUID(UUID sechubJobUUID) {
        this.sechubJobUUID = sechubJobUUID;
    }

    public void setCreated(LocalDateTime started) {
        this.created = started;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public UUID getSechubJobUUID() {
        return sechubJobUUID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sechubJobUUID);
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
        JobStatistic other = (JobStatistic) obj;
        return Objects.equals(sechubJobUUID, other.sechubJobUUID);
    }

    @Override
    public String toString() {
        return "JobStatistic [" + (sechubJobUUID != null ? "sechubJobUUID=" + sechubJobUUID + ", " : "")
                + (projectId != null ? "projectId=" + projectId + ", " : "") + (created != null ? "created=" + created + ", " : "")
                + (version != null ? "version=" + version : "") + "]";
    }

}
