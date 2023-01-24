// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.statistic.execution;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

/**
 * Represents a mapping between a scan, job and and job configuration. So its an
 * INTERNAL information
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = ExecutionStatisticData.TABLE_NAME)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class ExecutionStatisticData {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "STATISTIC_EXEC_DATA";

    public static final String COLUMN_EXECUTION_UUID = "EXECUTION_UUID";
    public static final String COLUMN_TYPE = "TYPE";
    public static final String COLUMN_KEY = "KEY";
    public static final String COLUMN_VALUE = "VALUE";

    public static final String COLUMN_STARTED = "STARTED";
    public static final String COLUMN_ENDED = "ENDED";
    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = ExecutionStatisticData.class.getSimpleName();

    public static final String PROPERTY_EXECUTION_UUID = "executionUUID";

    public static final String STATUS_STARTED = "STARTED";
    public static final String STATUS_OK = "OK";
    public static final String STATUS_FAILED = "FAILED";

    /* FIXME Albert Tregnaghi, 2023-01-24: clarify - combined fk or own uuid to have possibility for duplicated entries */
    @Id
    UUID uuid;
    
    @Column(name = COLUMN_EXECUTION_UUID, updatable = false, nullable = false, columnDefinition = "UUID")
    UUID executionUUID;

    @Column(name = COLUMN_TYPE)
    String type;
    
    @Column(name = COLUMN_KEY)
    String key;
    
    @Column(name = COLUMN_VALUE)
    String value;

    @Version
    @Column(name = "VERSION")
    Integer version;

    public ExecutionStatisticData() {
    }
    
    public void setType(String status) {
        this.type = status;
    }

    public UUID getExecutionUUID() {
        return executionUUID;
    }

}
