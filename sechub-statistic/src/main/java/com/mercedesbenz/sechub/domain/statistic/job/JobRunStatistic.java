// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.statistic.job;

import static jakarta.persistence.EnumType.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.model.TrafficLight;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Represents statistic data for job runs. See
 * /sechub-doc/src/docs/asciidoc/diagrams/diagram_em_statistic.puml for details
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = JobRunStatistic.TABLE_NAME)
@JsonIgnoreProperties(value = { "version" }, ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class JobRunStatistic {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "STATISTIC_JOB_RUN";

    public static final String COLUMN_EXECUTION_UUID = "EXECUTION_UUID";

    public static final String COLUMN_SECHUB_JOB_UUID = "SECHUB_JOB_UUID";
    public static final String COLUMN_PROJECT_ID = "PROJECT_ID";
    public static final String COLUMN_CREATED = "CREATED";
    public static final String COLUMN_STARTED = "STARTED";
    public static final String COLUMN_ENDED = "ENDED";
    public static final String COLUMN_FAILED = "FAILED";
    public static final String COLUMN_TRAFFIC_LIGHT = "TRAFFIC_LIGHT";
    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = JobRunStatistic.class.getSimpleName();

    public static final String PROPERTY_EXECUTION_UUID = "executionUUID";
    public static final String PROPERTY_SECHUB_JOB_UUID = "sechubJobUUID";

    public static final String PROPERTY_CREATED = "created";
    public static final String PROPERTY_STARTED = "started";
    public static final String PROPERTY_ENDED = "ended";
    public static final String PROPERTY_FAILED = "failed";
    public static final String PROPERTY_TRAFFIC_LIGHT = "trafficLight";

    @Id
    @Column(name = COLUMN_EXECUTION_UUID, updatable = false, nullable = false, columnDefinition = "UUID")
    UUID executionUUID;

    @Column(name = COLUMN_SECHUB_JOB_UUID, nullable = false, columnDefinition = "UUID")
    UUID sechubJobUUID;

    @Column(name = COLUMN_PROJECT_ID)
    String projectId;

    @Column(name = COLUMN_CREATED) // remark: we setup hibernate to use UTC settings - see application.properties
    LocalDateTime created;

    @Column(name = COLUMN_STARTED) // remark: we setup hibernate to use UTC settings - see application.properties
    LocalDateTime started;

    @Column(name = COLUMN_ENDED) // remark: we setup hibernate to use UTC settings - see application.properties
    LocalDateTime ended;

    @Column(name = COLUMN_FAILED)
    boolean failed;

    @Enumerated(STRING)
    @Column(name = COLUMN_TRAFFIC_LIGHT, nullable = true)
    TrafficLight trafficLight;

    @Version
    @Column(name = "VERSION")
    Integer version;

    public JobRunStatistic() {
    }

    public void setExecutionUUID(UUID executionUUID) {
        this.executionUUID = executionUUID;
    }

    public void setSechubJobUUID(UUID sechubJobUUID) {
        this.sechubJobUUID = sechubJobUUID;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setStarted(LocalDateTime started) {
        this.started = started;
    }

    public void setEnded(LocalDateTime ended) {
        this.ended = ended;
    }

    public void setTrafficLight(TrafficLight trafficLight) {
        this.trafficLight = trafficLight;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(executionUUID);
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
        JobRunStatistic other = (JobRunStatistic) obj;
        return Objects.equals(executionUUID, other.executionUUID);
    }

    @Override
    public String toString() {
        return "JobRunStatistic [" + (executionUUID != null ? "executionUUID=" + executionUUID + ", " : "")
                + (sechubJobUUID != null ? "sechubJobUUID=" + sechubJobUUID + ", " : "") + (projectId != null ? "projectId=" + projectId + ", " : "")
                + (created != null ? "created=" + created + ", " : "") + (started != null ? "started=" + started + ", " : "")
                + (ended != null ? "ended=" + ended + ", " : "") + (trafficLight != null ? "trafficLight=" + trafficLight + ", " : "")
                + (version != null ? "version=" + version : "") + "]";
    }

}
