// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.log;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mercedesbenz.sechub.commons.model.SecHubLocalDateTimeDeserializer;
import com.mercedesbenz.sechub.commons.model.SecHubLocalDateTimeSerializer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Represents a mapping between a scan, job and and job configuration. So its an
 * INTERNAL information
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = ProjectScanLog.TABLE_NAME)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class ProjectScanLog {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "SCAN_PROJECT_LOG";

    public static final String COLUMN_UUID = "UUID";
    public static final String COLUMN_PROJECT_ID = "PROJECT_ID";
    public static final String COLUMN_EXECUTED_BY = "EXECUTED_BY";
    public static final String COLUMN_SECHUB_JOB_UUID = "SECHUB_JOB_UUID";
    public static final String COLUMN_STATUS = "STATUS";

    public static final String COLUMN_STARTED = "STARTED";
    public static final String COLUMN_ENDED = "ENDED";
    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = ProjectScanLog.class.getSimpleName();

    public static final String PROPERTY_UUID = "uuid";
    public static final String PROPERTY_SECHUB_JOB_UUID = "sechubJobUUID";
    public static final String PROPERTY_EXECUTED_BY = "executedBy";
    public static final String PROPERTY_PROJECT_ID = "projectId";
    public static final String PROPERTY_STARTED = "started";
    public static final String PROPERTY_ENDED = "ended";
    public static final String PROPERTY_STATUS = "status";

    public static final String STATUS_STARTED = "STARTED";
    public static final String STATUS_OK = "OK";
    public static final String STATUS_FAILED = "FAILED";

    public static final String QUERY_DELETE_LOGS_OLDER_THAN = "DELETE FROM ProjectScanLog log WHERE log." + PROPERTY_STARTED + " < :cleanTimeStamp";;

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = COLUMN_UUID, updatable = false, nullable = false, columnDefinition = "UUID")
    UUID uUID;

    @Column(name = COLUMN_EXECUTED_BY)
    String executedBy;

    @Column(name = COLUMN_PROJECT_ID, nullable = false)
    String projectId;

    @Column(name = COLUMN_SECHUB_JOB_UUID, nullable = false, columnDefinition = "UUID")
    UUID sechubJobUUID;

    @Column(name = COLUMN_STATUS)
    String status;

    @Version
    @Column(name = "VERSION")
    Integer version;

    @JsonDeserialize(using = SecHubLocalDateTimeDeserializer.class)
    @JsonSerialize(using = SecHubLocalDateTimeSerializer.class)
    @Column(name = COLUMN_STARTED) // remark: we setup hibernate to use UTC settings - see application.properties
    LocalDateTime started;

    @JsonDeserialize(using = SecHubLocalDateTimeDeserializer.class)
    @JsonSerialize(using = SecHubLocalDateTimeSerializer.class)
    @Column(name = COLUMN_ENDED) // remark: we setup hibernate to use UTC settings - see application.properties
    LocalDateTime ended;

    ProjectScanLog() {
        // jpa only
    }

    public ProjectScanLog(String projectId, UUID sechubJobUUID, String executedBy) {
        this.projectId = projectId;
        this.sechubJobUUID = sechubJobUUID;
        this.executedBy = executedBy;

        this.started = LocalDateTime.now();
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStarted() {
        return started;
    }

    public void setEnded(LocalDateTime ended) {
        this.ended = ended;
    }

    public LocalDateTime getEnded() {
        return ended;
    }

    public UUID getUUID() {
        return uUID;
    }

    public UUID getSechubJobUUID() {
        return sechubJobUUID;
    }

    public String getExecutedBy() {
        return executedBy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ended, executedBy, projectId, sechubJobUUID, started, uUID, version);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProjectScanLog other = (ProjectScanLog) obj;
        return Objects.equals(ended, other.ended) && Objects.equals(executedBy, other.executedBy) && Objects.equals(projectId, other.projectId)
                && Objects.equals(sechubJobUUID, other.sechubJobUUID) && Objects.equals(started, other.started) && Objects.equals(uUID, other.uUID)
                && Objects.equals(version, other.version);
    }

    @Override
    public String toString() {
        return "ProjectScanLog [\nuUID=" + uUID + ", \nexecutedBy=" + executedBy + ", \nprojectId=" + projectId + ", \nsechubJobUUID=" + sechubJobUUID
                + ", \nstatus=" + status + ", \nstarted=" + started + ", \nended=" + ended + "\n]";
    }

}
