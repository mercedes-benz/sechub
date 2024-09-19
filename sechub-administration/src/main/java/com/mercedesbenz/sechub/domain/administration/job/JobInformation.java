// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.job;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;
import static jakarta.persistence.EnumType.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = JobInformation.TABLE_NAME)
@JsonIgnoreProperties("uuid")
public class JobInformation {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "ADM_JOB_INFORMATION";
    /**
     * Email address is also the primary key. So no duplicates
     */
    public static final String COLUMN_UUID = "UUID";
    public static final String COLUMN_JOB_UUID = "JOB_UUID";
    public static final String COLUMN_PROJECT_ID = "PROJECT_ID";
    public static final String COLUMN_OWNER = "OWNER";
    public static final String COLUMN_STATUS = "STATUS";
    /**
     * A generic time stamp - depends on current {@link JobStatus} . For status
     * "CREATED", this means creation time, for "RUNNING" the time when run started,
     * for "DONE" this field means the time stamp when it has been done
     */
    public static final String COLUMN_SINCE = "SINCE";
    /**
     * This is a free text field and shows up some information about
     */
    public static final String COLUMN_INFO = "INFO";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = JobInformation.class.getSimpleName();
    public static final String PROPERTY_UUID = "uUID";
    public static final String PROPERTY_JOB_UUID = "jobUUID";
    public static final String PROPERTY_STATUS = "status";

    public static final String PROPERTY_PROJECT_ID = "projectId";
    public static final String PROPERTY_OWNER = "owner";
    public static final String PROPERTY_SINCE = "since";

    public static final String QUERY_FIND_ALL_RUNNING_JOBS = "SELECT j FROM JobInformation j where j.status = com.mercedesbenz.sechub.domain.administration.job.JobStatus.RUNNING";
    public static final String QUERY_DELETE_JOBINFORMATION_FOR_JOBUUID = "DELETE FROM JobInformation j WHERE j.jobUUID=:jobUUID";

    public static final String QUERY_DELETE_JOBINFORMATION_OLDER_THAN = "DELETE FROM JobInformation j WHERE j." + PROPERTY_SINCE + " < :cleanTimeStamp";

    /* JPA only */
    JobInformation() {
    }

    public JobInformation(UUID jobUUID) {
        notNull(jobUUID, "SecHub job UUID may not be null!");

        this.jobUUID = jobUUID;
    }

    @Id
    @Column(name = COLUMN_JOB_UUID, unique = true, updatable = false, nullable = false, columnDefinition = "UUID")
    UUID jobUUID;

    @Column(name = COLUMN_PROJECT_ID, nullable = false)
    String projectId;

    @Column(name = COLUMN_OWNER, nullable = false)
    String owner;

    @Enumerated(STRING)
    @Column(name = COLUMN_STATUS, nullable = false)
    JobStatus status;

    @Column(name = COLUMN_SINCE) // remark: we setup hibernate to use UTC settings - see application.properties
    LocalDateTime since;

    @Column(name = COLUMN_INFO)
    String info;

    @Version
    @Column(name = "VERSION")
    Integer version;

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectId() {
        return projectId;
    }

    public UUID getJobUUID() {
        return jobUUID;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public LocalDateTime getSince() {
        return since;
    }

    public void setSince(LocalDateTime since) {
        this.since = since;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobUUID);
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
        JobInformation other = (JobInformation) obj;
        return Objects.equals(jobUUID, other.jobUUID);
    }

}
