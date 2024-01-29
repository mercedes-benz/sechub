// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import static javax.persistence.EnumType.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;

import com.mercedesbenz.sechub.commons.model.ModuleGroup;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.commons.model.job.ExecutionResult;
import com.mercedesbenz.sechub.commons.model.job.ExecutionState;

/**
 * Represents a JOB in SecHub. We did not name it as Job because of Spring batch
 * has already a Job class which did confuse.
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = ScheduleSecHubJob.TABLE_NAME)
public class ScheduleSecHubJob {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "SCHEDULE_SECHUB_JOB";

    public static final String COLUMN_UUID = "UUID";
    public static final String COLUMN_OWNER = "OWNER";
    public static final String COLUMN_CREATED = "CREATED";
    public static final String COLUMN_STARTED = "STARTED";
    public static final String COLUMN_ENDED = "ENDED";
    public static final String COLUMN_STATE = "STATE";
    public static final String COLUMN_CONFIGURATION = "CONFIGURATION";
    public static final String COLUMN_TRAFFIC_LIGHT = "TRAFFIC_LIGHT";
    public static final String COLUMN_MODULE_GROUP = "MODULE_GROUP";

    public static final String COLUMN_PROJECT_ID = "PROJECT_ID";

    public static final String COLUMN_RESULT = "RESULT";

    public static final String COLUMN_MESSAGES = "MESSAGES";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = "ScheduleSecHubJob";

    public static final String PROPERTY_EXECUTION_STATE = "executionState";
    public static final String PROPERTY_EXECUTION_RESULT = "executionResult";
    public static final String PROPERTY_PROJECT_ID = "projectId";
    public static final String PROPERTY_UUID = "uUID";
    public static final String PROPERTY_OWNER = "owner";
    public static final String PROPERTY_CREATED = "created";
    public static final String PROPERTY_STARTED = "started";
    public static final String PROPERTY_ENDED = "ended";
    public static final String PROPERTY_MESSAGES = "jsonMessages";
    public static final String PROPERTY_MODULE_GROUP = "moduleGroup";
    public static final String PROPERTY_DATA = "data";

    public static final String QUERY_DELETE_JOB_OLDER_THAN = "DELETE FROM ScheduleSecHubJob j WHERE j." + PROPERTY_CREATED + " <:cleanTimeStamp";

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = COLUMN_UUID, updatable = false, nullable = false, columnDefinition = "UUID")
    UUID uUID;

    @Column(name = COLUMN_PROJECT_ID, nullable = false)
    String projectId;

    @Column(name = COLUMN_OWNER, nullable = false)
    String owner;

    @Column(name = COLUMN_CREATED, nullable = false) // remark: we setup hibernate to use UTC settings - see
    LocalDateTime created;

    @Column(name = COLUMN_STARTED) // remark: we setup hibernate to use UTC settings - see application.properties
    LocalDateTime started;

    @Column(name = COLUMN_ENDED) // remark: we setup hibernate to use UTC settings - see application.properties
    LocalDateTime ended;

    @Column(name = COLUMN_CONFIGURATION)
    String jsonConfiguration;

    @Enumerated(STRING)
    @Column(name = COLUMN_STATE, nullable = false)
    ExecutionState executionState = ExecutionState.INITIALIZING;

    @Enumerated(STRING)
    @Column(name = COLUMN_RESULT, nullable = false)
    ExecutionResult executionResult = ExecutionResult.NONE;

    @Enumerated(STRING)
    @Column(name = COLUMN_TRAFFIC_LIGHT, nullable = true)
    TrafficLight trafficLight;

    @Enumerated(STRING)
    @Column(name = COLUMN_MODULE_GROUP, nullable = true) // nullable only for backward compatibility with old jobs
    ModuleGroup moduleGroup;

    @Column(name = COLUMN_MESSAGES)
    private String jsonMessages;

    @OneToMany(cascade = { CascadeType.ALL }, mappedBy = ScheduleSecHubJobData.PROPERTY_JOB_UUID, orphanRemoval = true)
    Set<ScheduleSecHubJobData> data = new HashSet<>();

    @Version
    @Column(name = "VERSION")
    Integer version;

    public void setExecutionState(ExecutionState executionState) {
        if (executionState == null) {
            this.executionState = ExecutionState.INITIALIZING;
        } else {
            this.executionState = executionState;
        }
    }

    public void setExecutionResult(ExecutionResult executionResult) {
        this.executionResult = executionResult;
    }

    public void setTrafficLight(TrafficLight trafficLight) {
        this.trafficLight = trafficLight;
    }

    public void setStarted(LocalDateTime started) {
        this.started = started;
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

    public void setOwner(String createdBy) {
        this.owner = createdBy;
    }

    public String getOwner() {
        return owner;
    }

    public String getProjectId() {
        return projectId;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public String getJsonConfiguration() {
        return jsonConfiguration;
    }

    public ExecutionState getExecutionState() {
        return executionState;
    }

    public ExecutionResult getExecutionResult() {
        return executionResult;
    }

    public TrafficLight getTrafficLight() {
        return trafficLight;
    }

    public String getJsonMessages() {
        return jsonMessages;
    }

    public void setJsonMessages(String jsonMessages) {
        this.jsonMessages = jsonMessages;
    }

    public void setModuleGroup(ModuleGroup moduleGroup) {
        this.moduleGroup = moduleGroup;
    }

    public ModuleGroup getModuleGroup() {
        return moduleGroup;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uUID == null) ? 0 : uUID.hashCode());
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
        ScheduleSecHubJob other = (ScheduleSecHubJob) obj;
        return Objects.equals(uUID, other.uUID);
    }

    /**
     * Adds job data. The job data will have the same creation time as the job
     * itself.
     *
     * @param key
     * @param value
     */
    public void addData(String key, String value) {
        ScheduleSecHubJobData jobData = new ScheduleSecHubJobData(uUID, key, value);
        jobData.created = created; // we sync the creation time - avoids potential conflicts with deleteOlderThan
        data.add(jobData);
    }

}
