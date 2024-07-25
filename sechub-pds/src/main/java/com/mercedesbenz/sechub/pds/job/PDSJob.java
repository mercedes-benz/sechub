// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static jakarta.persistence.EnumType.STRING;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mercedesbenz.sechub.commons.model.SecHubLocalDateTimeDeserializer;
import com.mercedesbenz.sechub.commons.model.SecHubLocalDateTimeSerializer;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Represents a PDS Job which contains information about ownership, related
 * sechub job and also state,configuration and last but not least the result of
 * the job.
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = PDSJob.TABLE_NAME)
public class PDSJob {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "PDS_JOB";

    public static final String COLUMN_UUID = "UUID";

    public static final String COLUMN_SERVER_ID = "SERVER_ID";
    public static final String COLUMN_STATE = "STATE";
    public static final String COLUMN_OWNER = "OWNER";

    public static final String COLUMN_CREATED = "CREATED";
    public static final String COLUMN_STARTED = "STARTED";
    public static final String COLUMN_ENDED = "ENDED";

    public static final String COLUMN_CONFIGURATION = "CONFIGURATION";

    public static final String COLUMN_RESULT = "RESULT";

    public static final String COLUMN_ERROR_STREAM_TEXT = "ERROR_STREAM_TEXT";

    public static final String COLUMN_OUTPUT_STREAM_TEXT = "OUTPUT_STREAM_TEXT";

    public static final String COLUMN_MESSAGES = "MESSAGES";

    public static final String COLUMN_META_DATA = "META_DATA";

    public static final String COLUMN_LAST_STREAM_TEXT_REFRESH_REQUEST = "LAST_STREAM_TEXT_REFRESH_REQUEST";
    public static final String COLUMN_LAST_STREAM_TEXT_UPDATE = "LAST_STREAM_TEXT_UPDATE";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = PDSJob.class.getSimpleName();

    public static final String PROPERTY_UUID = "uUID";
    public static final String PROPERTY_SERVER_ID = "serverId";

    public static final String PROPERTY_STATE = "state";
    public static final String PROPERTY_OWNER = "owner";

    public static final String PROPERTY_CREATED = "created";
    public static final String PROPERTY_STARTED = "started";
    public static final String PROPERTY_ENDED = "ended";

    public static final String PROPERTY_CONFIGURATION = "configuration";
    public static final String PROPERTY_RESULT = "result";

    public static final String QUERY_DELETE_JOB_OLDER_THAN = "DELETE FROM PDSJob j WHERE j." + PROPERTY_CREATED + " < :cleanTimeStamp";

    public static final String QUERY_FIND_JOBS_IN_STATE = "SELECT j from PDSJob j WHERE j." + PROPERTY_STATE + "= :statusState";

    public static final String QUERY_FORCE_JOB_STATE_UPDATE = "UPDATE PDSJob j set j." + PROPERTY_STATE + "=:statusState WHERE j." + PROPERTY_UUID
            + " in :jobUUIDs";

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = COLUMN_UUID, updatable = false, nullable = false, columnDefinition = "UUID")
    UUID uUID;

    @Column(name = COLUMN_OWNER, nullable = false)
    String owner;

    /**
     * Server ID is used to give possibilty to use a shared database for multiple
     * PDS clusters. Members of cluster use the same server id, so scheduling etc.
     * is working well even when multiple PDS are running
     */
    @Column(name = COLUMN_SERVER_ID, nullable = false)
    String serverId;

    @Column(name = COLUMN_CREATED, nullable = false) // remark: we setup hibernate to use UTC settings - see
    @JsonDeserialize(using = SecHubLocalDateTimeDeserializer.class)
    @JsonSerialize(using = SecHubLocalDateTimeSerializer.class)
    LocalDateTime created;

    @Column(name = COLUMN_STARTED) // remark: we setup hibernate to use UTC settings - see application.properties
    @JsonDeserialize(using = SecHubLocalDateTimeDeserializer.class)
    @JsonSerialize(using = SecHubLocalDateTimeSerializer.class)
    LocalDateTime started;

    @Column(name = COLUMN_ENDED) // remark: we setup hibernate to use UTC settings - see application.properties
    @JsonDeserialize(using = SecHubLocalDateTimeDeserializer.class)
    @JsonSerialize(using = SecHubLocalDateTimeSerializer.class)
    LocalDateTime ended;

    @Column(name = COLUMN_LAST_STREAM_TEXT_REFRESH_REQUEST) // remark: we setup hibernate to use UTC settings - see application.properties
    @JsonDeserialize(using = SecHubLocalDateTimeDeserializer.class)
    @JsonSerialize(using = SecHubLocalDateTimeSerializer.class)
    LocalDateTime lastStreamTextRefreshRequest;

    @Column(name = COLUMN_LAST_STREAM_TEXT_UPDATE) // remark: we setup hibernate to use UTC settings - see application.properties
    @JsonDeserialize(using = SecHubLocalDateTimeDeserializer.class)
    @JsonSerialize(using = SecHubLocalDateTimeSerializer.class)
    LocalDateTime lastStreamTextUpdate;

    @Column(name = COLUMN_CONFIGURATION)
    String jsonConfiguration;

    @Column(name = COLUMN_RESULT)
    @JdbcTypeCode(Types.LONGNVARCHAR) // why not using @Lob, because hibernate/postgres issues. see
    // https://stackoverflow.com/questions/25094410/hibernate-error-while-persisting-text-datatype?noredirect=1#comment39048566_25094410
    String result;

    @Column(name = COLUMN_ERROR_STREAM_TEXT)
    @JdbcTypeCode(Types.LONGNVARCHAR) // see remarks on COLUMN_RESULT
    String errorStreamText;

    @Column(name = COLUMN_OUTPUT_STREAM_TEXT)
    @JdbcTypeCode(Types.LONGNVARCHAR) // see remarks on COLUMN_RESULT
    String outputStreamText;

    @Enumerated(STRING)
    @Column(name = COLUMN_STATE, nullable = false)
    PDSJobStatusState state = PDSJobStatusState.CREATED;

    @Version
    @Column(name = "VERSION")
    Integer version;

    @Column(name = COLUMN_MESSAGES)
    @JdbcTypeCode(Types.LONGNVARCHAR) // see remarks on COLUMN_RESULT
    String messages;

    @Column(name = COLUMN_META_DATA)
    @JdbcTypeCode(Types.LONGNVARCHAR) // see remarks on COLUMN_RESULT
    String metaDataText;

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setState(PDSJobStatusState executionResult) {
        this.state = executionResult;
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

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public String getJsonConfiguration() {
        return jsonConfiguration;
    }

    public PDSJobStatusState getState() {
        return state;
    }

    public String getResult() {
        return result;
    }

    public LocalDateTime getLastStreamTextRefreshRequest() {
        return lastStreamTextRefreshRequest;
    }

    public LocalDateTime getLastStreamTextUpdate() {
        return lastStreamTextUpdate;
    }

    public String getOutputStreamText() {
        return outputStreamText;
    }

    public String getErrorStreamText() {
        return errorStreamText;
    }

    public String getMessages() {
        return messages;
    }

    public String getMetaDataText() {
        return metaDataText;
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
        PDSJob other = (PDSJob) obj;
        return Objects.equals(uUID, other.uUID);
    }

}
