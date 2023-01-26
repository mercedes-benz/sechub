// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.statistic.job;

import static javax.persistence.EnumType.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import com.mercedesbenz.sechub.commons.model.TrafficLight;

/**
 * Represents statistic data for job runs. See
 * /sechub-doc/src/docs/asciidoc/diagrams/diagram_em_statistic.puml for details
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = JobRunStatistic.TABLE_NAME)
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
