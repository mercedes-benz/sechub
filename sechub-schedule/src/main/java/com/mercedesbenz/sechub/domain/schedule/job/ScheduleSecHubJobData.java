// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Represents additional (internal) JOB data in SecHub.
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = ScheduleSecHubJobData.TABLE_NAME)
@IdClass(ScheduleSecHubJobDataId.class)
public class ScheduleSecHubJobData {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "SCHEDULE_SECHUB_JOB_DATA";

    public static final String COLUMN_JOB_UUID = "JOB_UUID";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_VAL = "VAL";
    public static final String COLUMN_CREATED = "CREATED";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = "ScheduleSecHubJobData";
    public static final String PROPERTY_JOB_UUID = "jobUUID";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_VALUE = "value";
    public static final String PROPERTY_CREATED = "created";

    public static final String QUERY_DELETE_JOB_DATA_OLDER_THAN = "DELETE FROM ScheduleSecHubJobData d WHERE d." + PROPERTY_CREATED + " <:cleanTimeStamp";

    @Id
    @Column(name = COLUMN_JOB_UUID, updatable = false, nullable = false, columnDefinition = "UUID")
    UUID jobUUID;

    @Id
    @Column(name = COLUMN_ID, nullable = false)
    String id;

    @Column(name = COLUMN_VAL)
    String value;

    @Version
    @Column(name = "VERSION")
    Integer version;

    @Column(name = COLUMN_CREATED)
    LocalDateTime created; // necessary for deleteOlderTahn...

    ScheduleSecHubJobData() {
        // jpa only
    }

    ScheduleSecHubJobData(UUID jobUUID, String id, String value) {
        this.jobUUID = jobUUID;
        this.id = id;
        this.value = value;
        this.created = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public UUID getJobUUID() {
        return jobUUID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobUUID, id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScheduleSecHubJobData other = (ScheduleSecHubJobData) obj;
        return Objects.equals(jobUUID, other.jobUUID) && Objects.equals(id, other.id);
    }

}