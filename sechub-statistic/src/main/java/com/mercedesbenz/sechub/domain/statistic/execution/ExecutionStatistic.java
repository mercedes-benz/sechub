// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.statistic.execution;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mercedesbenz.sechub.commons.model.LocalDateTimeDeserializer;
import com.mercedesbenz.sechub.commons.model.LocalDateTimeSerializer;

/**
 * Statistic table about execution phase with common data for the same execution uuid (PK)
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = ExecutionStatistic.TABLE_NAME)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class ExecutionStatistic {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "STATISTIC_EXEC";

    public static final String COLUMN_EXECUTION_UUID = "EXECUTION_UUID";
    public static final String COLUMN_SECHUB_JOB_UUID = "SECHUB_JOB_UUID";
    public static final String COLUMN_STATUS = "STATUS";

    public static final String COLUMN_STARTED = "STARTED";
    public static final String COLUMN_ENDED = "ENDED";
    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = ExecutionStatistic.class.getSimpleName();

    public static final String PROPERTY_SECHUB_JOB_UUID = "sechubJobUUID";
    public static final String PROPERTY_STARTED = "started";
    public static final String PROPERTY_ENDED = "ended";
    public static final String PROPERTY_STATUS = "status";

    public static final String STATUS_STARTED = "STARTED";
    public static final String STATUS_OK = "OK";
    public static final String STATUS_FAILED = "FAILED";

    @Id
    @Column(name = COLUMN_EXECUTION_UUID, updatable = false, nullable = false, columnDefinition = "UUID")
    UUID executionUUID;

    @Column(name = COLUMN_SECHUB_JOB_UUID, nullable = false, columnDefinition = "UUID")
    UUID sechubJobUUID;

    @Column(name = COLUMN_STATUS)
    String status;

    @Version
    @Column(name = "VERSION")
    Integer version;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @Column(name = COLUMN_STARTED) // remark: we setup hibernate to use UTC settings - see application.properties
    LocalDateTime started;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @Column(name = COLUMN_ENDED) // remark: we setup hibernate to use UTC settings - see application.properties
    LocalDateTime ended;

    public ExecutionStatistic() {
    }
    
    public void setSechubJobUUID(UUID sechubJobUUID) {
        this.sechubJobUUID = sechubJobUUID;
    }
    
    public void setStarted(LocalDateTime started) {
        this.started = started;
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

    public UUID getExecutionUUID() {
        return executionUUID;
    }
    
    public UUID getSechubJobUUID() {
        return sechubJobUUID;
    }


}
