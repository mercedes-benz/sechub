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
 * /** Represents statistic data for a job - contains only parts which will not
 * change on job runs. See
 * /sechub-doc/src/docs/asciidoc/diagrams/diagram_em_statistic.puml for details
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = JobRunStatisticData.TABLE_NAME)
public class JobRunStatisticData {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "STATISTIC_JOB_RUN_DATA";

    public static final String COLUMN_UUID = "UUID";

    public static final String COLUMN_EXECUTION_UUID = "EXECUTION_UUID";
    public static final String COLUMN_TYPE = "TYPE";
    public static final String COLUMN_KEY = "KEY";
    public static final String COLUMN_VALUE = "VALUE";
    public static final String COLUMN_TIMESTAMP = "TIMESTAMP";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = JobRunStatisticData.class.getSimpleName();

    public static final String PROPERTY_UUID = "uuid";

    public static final String PROPERTY_EXECUTION_UUID = "executionUUID";
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_KEY = "key";
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
    private JobRunStatisticDataType type;

    @Enumerated(STRING)
    @Column(name = COLUMN_KEY)
    String key;

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

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

}
