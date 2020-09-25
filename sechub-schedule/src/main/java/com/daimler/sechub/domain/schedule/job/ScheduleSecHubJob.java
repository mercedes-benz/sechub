// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.job;

import static javax.persistence.EnumType.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;

import com.daimler.sechub.commons.model.TrafficLight;
import com.daimler.sechub.domain.schedule.ExecutionResult;
import com.daimler.sechub.domain.schedule.ExecutionState;

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

	public static final String COLUMN_PROJECT_ID = "PROJECT_ID";

	public static final String COLUMN_RESULT = "RESULT";

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

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = COLUMN_UUID, updatable = false, nullable = false)
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
	@Column(name = COLUMN_TRAFFIC_LIGHT, nullable = true) // HIBERNATE 5.2 would support Optional, but currently we use
															// 5.0.1
	TrafficLight trafficLight;

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

}
