// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.statistic.job;

import static javax.persistence.EnumType.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

/**
 * /** Represents statistic data for a job - available outside execution phase.
 * See /sechub-doc/src/docs/asciidoc/diagrams/diagram_em_statistic.puml for
 * details
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = JobStatisticData.TABLE_NAME)
public class JobStatisticData {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "STATISTIC_JOB_DATA";

    public static final String COLUMN_UUID = "UUID";

    public static final String COLUMN_SECHUB_JOB_UUID = "SECHUB_JOB_UUID";
    public static final String COLUMN_TYPE = "TYPE";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_VALUE = "VAL"; // H2 does complain about "value"... so we use "val"
    public static final String COLUMN_TIMESTAMP = "TIMESTAMP";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = JobStatisticData.class.getSimpleName();

    public static final String PROPERTY_UUID = "uuid";
    public static final String PROPERTY_SECHUB_JOB_UUID = "sechubJobUUID";

    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_VALUE = "value";
    public static final String PROPERTY_TIMESTAMP = "timeStamp";

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = COLUMN_UUID, updatable = false, nullable = false, columnDefinition = "UUID")
    UUID uUID;

    @Column(name = COLUMN_SECHUB_JOB_UUID, updatable = false, nullable = false, columnDefinition = "UUID")
    UUID sechubJobUUID;

    @Enumerated(STRING)
    @Column(name = COLUMN_TYPE, nullable = false)
    private JobStatisticDataType type;

    @Enumerated(STRING)
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

    public JobStatisticData() {
    }

    public void setType(JobStatisticDataType type) {
        this.type = type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public void setSechubJobUUID(UUID sechubJobUUID) {
        this.sechubJobUUID = sechubJobUUID;
    }

}
