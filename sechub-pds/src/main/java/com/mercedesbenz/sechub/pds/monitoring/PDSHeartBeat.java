// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.monitoring;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = PDSHeartBeat.TABLE_NAME)
public class PDSHeartBeat {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "PDS_HEARTBEAT";

    public static final String COLUMN_UUID = "UUID";

    public static final String COLUMN_SERVER_ID = "SERVER_ID";

    public static final String COLUMN_RESULT = "RESULT";

    public static final String COLUMN_UPDATED = "UPDATED";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = PDSHeartBeat.class.getSimpleName();

    public static final String PROPERTY_UUID = "uUID";
    public static final String PROPERTY_SERVER_ID = "serverId";
    public static final String PROPERTY_RESULT = "result";

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = COLUMN_UUID, updatable = false, nullable = false, columnDefinition = "UUID")
    UUID uUID;

    /**
     * Server ID is used to give possibilty to use a shared database for multiple
     * PDS clusters. Members of cluster use the same server id, so scheduling etc.
     * is working well even when multiple PDS are running
     */
    @Column(name = COLUMN_SERVER_ID, nullable = false)
    String serverId;

    @Column(name = COLUMN_RESULT)
    @JdbcTypeCode(Types.LONGNVARCHAR) // why not using @Lob, because hibernate/postgres issues. see
    // https://stackoverflow.com/questions/25094410/hibernate-error-while-persisting-text-datatype?noredirect=1#comment39048566_25094410
    String clusterMemberData;

    @Version
    @Column(name = "VERSION")
    Integer version;

    @Column(name = COLUMN_UPDATED)
    LocalDateTime updated;

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setClusterMemberData(String clusterMemberData) {
        this.clusterMemberData = clusterMemberData;
    }

    public UUID getUUID() {
        return uUID;
    }

    public String getClusterMemberData() {
        return clusterMemberData;
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
        PDSHeartBeat other = (PDSHeartBeat) obj;
        return Objects.equals(uUID, other.uUID);
    }

}
