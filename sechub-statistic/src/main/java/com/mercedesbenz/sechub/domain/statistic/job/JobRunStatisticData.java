// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.statistic.job;

import static jakarta.persistence.EnumType.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * /** Represents statistic data for a job - contains only parts which will not
 * change on job runs. See
 * /sechub-doc/src/docs/asciidoc/diagrams/diagram_em_statistic.puml for details
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = JobRunStatisticData.TABLE_NAME)
@JsonIgnoreProperties(value = { "version", "uuid" }, ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class JobRunStatisticData {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "STATISTIC_JOB_RUN_DATA";

    public static final String COLUMN_UUID = "UUID";

    public static final String COLUMN_EXECUTION_UUID = "EXECUTION_UUID";
    public static final String COLUMN_TYPE = "TYPE";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_VALUE = "VAL"; // H2 does complain about "value"... so we use "val"
    public static final String COLUMN_TIMESTAMP = "TIMESTAMP";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = JobRunStatisticData.class.getSimpleName();

    public static final String PROPERTY_UUID = "uuid";

    public static final String PROPERTY_EXECUTION_UUID = "executionUUID";
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_VALUE = "value";

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = COLUMN_UUID, updatable = false, nullable = false, columnDefinition = "UUID")
    UUID uUID;

    @Column(name = COLUMN_EXECUTION_UUID, updatable = false, nullable = false, columnDefinition = "UUID")
    UUID executionUUID;

    @Enumerated(STRING)
    @Column(name = COLUMN_TYPE, nullable = false)
    JobRunStatisticDataType type;

    @Column(name = COLUMN_ID)
    String id;

    @Column(name = COLUMN_VALUE, nullable = false)
    BigInteger value;

    @CreationTimestamp
    @Column(name = COLUMN_TIMESTAMP) // remark: we setup hibernate to use UTC settings - see application.properties
    LocalDateTime timeStamp;

    @Version
    @Column(name = "VERSION")
    Integer version;

    public JobRunStatisticData() {
    }

    public void setExecutionUUID(UUID executionUUID) {
        this.executionUUID = executionUUID;
    }

    public void setType(JobRunStatisticDataType type) {
        this.type = type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uUID);
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
        JobRunStatisticData other = (JobRunStatisticData) obj;
        return Objects.equals(uUID, other.uUID);
    }

    @Override
    public String toString() {
        return "JobRunStatisticData [" + (uUID != null ? "uUID=" + uUID + ", " : "") + (executionUUID != null ? "executionUUID=" + executionUUID + ", " : "")
                + (type != null ? "type=" + type + ", " : "") + (id != null ? "id=" + id + ", " : "") + (value != null ? "value=" + value + ", " : "")
                + (timeStamp != null ? "timeStamp=" + timeStamp + ", " : "") + (version != null ? "version=" + version : "") + "]";
    }

}
