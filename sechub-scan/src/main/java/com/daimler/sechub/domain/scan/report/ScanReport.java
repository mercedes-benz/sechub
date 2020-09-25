// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.report;

import static javax.persistence.EnumType.*;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import com.daimler.sechub.commons.model.TrafficLight;

@Entity
@Table(name = ScanReport.TABLE_NAME)
public class ScanReport {

	/* +-----------------------------------------------------------------------+ */
	/* +............................ SQL ......................................+ */
	/* +-----------------------------------------------------------------------+ */
	public static final String TABLE_NAME = "SCAN_REPORT";

	public static final String COLUMN_UUID = "UUID";
	public static final String COLUMN_SECHUB_JOB_UUID = "SECHUB_JOB_UUID";

	public static final String COLUMN_RESULT = "RESULT";
	public static final String COLUMN_TRAFFIC_LIGHT = "TRAFFIC_LIGHT";
	public static final String COLUMN_PROJECT_ID = "PROJECT_ID";

	public static final String COLUMN_STARTED = "STARTED";
	public static final String COLUMN_ENDED = "ENDED";

	/* +-----------------------------------------------------------------------+ */
	/* +............................ JPQL .....................................+ */
	/* +-----------------------------------------------------------------------+ */
	public static final String CLASS_NAME = ScanReport.class.getSimpleName();

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = COLUMN_UUID, updatable = false, nullable = false)
	UUID uUID;

	@Column(name = COLUMN_SECHUB_JOB_UUID)
	private UUID secHubJobUUID; // no referential integrity - only as information for report collecting
								// necessary

	@Type(type = "text") // why not using @Lob, because hibernate/postgres issues. see
							// https://stackoverflow.com/questions/25094410/hibernate-error-while-persisting-text-datatype?noredirect=1#comment39048566_25094410
	@Column(name = COLUMN_RESULT)
	private String result;


	@Enumerated(STRING)
	@Column(name = COLUMN_TRAFFIC_LIGHT, nullable = true)
	private TrafficLight trafficLight;

	@Column(name = COLUMN_PROJECT_ID, nullable = false) String projectId;

	@Column(name = COLUMN_STARTED) // remark: we setup hibernate to use UTC settings - see application.properties
	LocalDateTime started;

	@Column(name = COLUMN_ENDED) // remark: we setup hibernate to use UTC settings - see application.properties
	LocalDateTime ended;

	@Version
	@Column(name = "VERSION")
	Integer version;

	ScanReport() {
		// JPA only
	}

	public ScanReport(UUID secHubJobUUID, String projectId) {
		this.secHubJobUUID = secHubJobUUID;
		this.projectId = projectId;
	}

	public UUID getSecHubJobUUID() {
		return secHubJobUUID;
	}

	public String getResult() {
		return result;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setTrafficLight(TrafficLight trafficLight) {
		this.trafficLight = trafficLight;
	}

	public String getTrafficLightAsString() {
		if (trafficLight == null) {
			return null;
		}
		return trafficLight.name();
	}

	public UUID getUUID() {
		return uUID;
	}

	public void setResult(String result) {
		this.result = result;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uUID == null) ? 0 : uUID.hashCode());
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
		ScanReport other = (ScanReport) obj;
		if (uUID == null) {
			if (other.uUID != null)
				return false;
		} else if (!uUID.equals(other.uUID))
			return false;
		return true;
	}
}
