// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

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
import org.hibernate.annotations.Type;

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
    public static final String COLUMN_SECHUB_JOB_UUID = "SECHUB_JOB_UUID";

    public static final String COLUMN_SERVER_ID = "SERVER_ID";
    public static final String COLUMN_STATE = "STATE";
    public static final String COLUMN_OWNER = "OWNER";

    public static final String COLUMN_CREATED = "CREATED";
    public static final String COLUMN_STARTED = "STARTED";
    public static final String COLUMN_ENDED = "ENDED";

    public static final String COLUMN_CONFIGURATION = "CONFIGURATION";

    public static final String COLUMN_RESULT = "RESULT";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = PDSJob.class.getSimpleName();

    public static final String PROPERTY_UUID = "uUID";
    public static final String PROPERTY_SERVER_ID = "serverId";
    public static final String PROPERTY_SECHUB_JOB_UUID = "sechubJobUUID";

    public static final String PROPERTY_STATE = "state";
    public static final String PROPERTY_OWNER = "owner";

    public static final String PROPERTY_CREATED = "created";
    public static final String PROPERTY_STARTED = "started";
    public static final String PROPERTY_ENDED = "ended";

    public static final String PROPERTY_CONFIGURATION = "configuration";
    public static final String PROPERTY_RESULT = "result";

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = COLUMN_UUID, updatable = false, nullable = false)
    UUID uUID;

    @Column(name = COLUMN_OWNER, nullable = false)
    String owner;
    
    /**
     * Server ID is used to give possibilty to use a shared database for multiple PDS clusters.
     * Members of cluster use the same server id, so scheduling etc. is working well even when
     * multiple PDS are running
     */
    @Column(name = COLUMN_SERVER_ID, nullable = false)
    String serverId;

    @Column(name = COLUMN_CREATED, nullable = false) // remark: we setup hibernate to use UTC settings - see
    LocalDateTime created;

    @Column(name = COLUMN_STARTED) // remark: we setup hibernate to use UTC settings - see application.properties
    LocalDateTime started;

    @Column(name = COLUMN_ENDED) // remark: we setup hibernate to use UTC settings - see application.properties
    LocalDateTime ended;

    @Column(name = COLUMN_CONFIGURATION)
    String jsonConfiguration;

    @Column(name = COLUMN_RESULT)
    @Type(type = "text") // why not using @Lob, because hibernate/postgres issues. see
    // https://stackoverflow.com/questions/25094410/hibernate-error-while-persisting-text-datatype?noredirect=1#comment39048566_25094410
    String result;

    @Enumerated(STRING)
    @Column(name = COLUMN_STATE, nullable = false)
    PDSJobStatusState state = PDSJobStatusState.CREATED;

    @Version
    @Column(name = "VERSION")
    Integer version;

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
