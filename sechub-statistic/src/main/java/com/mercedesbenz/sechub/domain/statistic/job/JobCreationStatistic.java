// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.statistic.job;

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
 * Represents statistic data about a job. When a job is ececuted multiple times we will have still
 * only ONE entry for the job here.
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = JobCreationStatistic.TABLE_NAME)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class JobCreationStatistic {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "STATISTIC_JOB_CREATION";

    public static final String COLUMN_PROJECT_ID = "PROJECT_ID";
    public static final String COLUMN_SECHUB_JOB_UUID = "SECHUB_JOB_UUID";

    public static final String COLUMN_CREATED = "CREATED";
    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = JobCreationStatistic.class.getSimpleName();

    public static final String PROPERTY_UUID = "uuid";
    public static final String PROPERTY_SECHUB_JOB_UUID = "sechubJobUUID";
    public static final String PROPERTY_PROJECT_ID = "projectId";
    public static final String PROPERTY_CREATED = "created";

    @Id
    @Column(name = COLUMN_SECHUB_JOB_UUID, nullable = false, columnDefinition = "UUID")
    UUID sechubJobUUID;
    
    @Column(name = COLUMN_PROJECT_ID, nullable = false)
    String projectId;

    @Version
    @Column(name = "VERSION")
    Integer version;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @Column(name = COLUMN_CREATED) // remark: we setup hibernate to use UTC settings - see application.properties
    LocalDateTime created;

    public JobCreationStatistic() {
    }
    
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    
    public void setSechubJobUUID(UUID sechubJobUUID) {
        this.sechubJobUUID = sechubJobUUID;
    }
    
    public void setCreated(LocalDateTime started) {
        this.created = started;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public UUID getSechubJobUUID() {
        return sechubJobUUID;
    }


}
