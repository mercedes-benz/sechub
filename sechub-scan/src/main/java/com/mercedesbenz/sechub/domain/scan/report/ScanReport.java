// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import static jakarta.persistence.EnumType.STRING;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;

import com.mercedesbenz.sechub.commons.model.TrafficLight;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = ScanReport.TABLE_NAME)
public class ScanReport {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "SCAN_REPORT";

    public static final String COLUMN_UUID = "UUID";
    public static final String COLUMN_SECHUB_JOB_UUID = "SECHUB_JOB_UUID";

    public static final String COLUMN_RESULT = "RESULT";
    public static final String COLUMN_RESULT_TYPE = "RESULT_TYPE";
    public static final String COLUMN_TRAFFIC_LIGHT = "TRAFFIC_LIGHT";
    public static final String COLUMN_PROJECT_ID = "PROJECT_ID";

    public static final String COLUMN_STARTED = "STARTED";
    public static final String COLUMN_ENDED = "ENDED";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = ScanReport.class.getSimpleName();
    public static final String PROPERTY_REPORT_STARTED = "started";

    public static final String QUERY_DELETE_REPORTS_OLDER_THAN = "DELETE FROM ScanReport r WHERE r." + PROPERTY_REPORT_STARTED + " < :cleanTimeStamp";

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = COLUMN_UUID, updatable = false, nullable = false, columnDefinition = "UUID")
    UUID uUID;

    @Column(name = COLUMN_SECHUB_JOB_UUID, columnDefinition = "UUID")
    UUID secHubJobUUID; // no referential integrity - only as information for report collecting
                        // necessary

    @JdbcTypeCode(Types.LONGVARCHAR) // why not using @Lob, because hibernate/postgres issues. see
                                     // https://stackoverflow.com/questions/25094410/hibernate-error-while-persisting-text-datatype?noredirect=1#comment39048566_25094410
                                     // In Hibernate 6: https://stackoverflow.com/a/74602072
    @Column(name = COLUMN_RESULT)
    private String result;

    @Enumerated(STRING)
    @Column(name = COLUMN_RESULT_TYPE)
    private ScanReportResultType resultType;

    @Enumerated(STRING)
    @Column(name = COLUMN_TRAFFIC_LIGHT, nullable = true)
    private TrafficLight trafficLight;

    @Column(name = COLUMN_PROJECT_ID, nullable = false)
    String projectId;

    @Column(name = COLUMN_STARTED) // remark: we setup hibernate to use UTC settings - see application.properties
    LocalDateTime started;

    @Column(name = COLUMN_ENDED) // remark: we setup hibernate to use UTC settings - see application.properties
    LocalDateTime ended;

    @Version
    @Column(name = "VERSION")
    Integer version;

    ScanReport() {
        // JPA only
    }

    public ScanReport(UUID secHubJobUUID, String projectId) {
        this.secHubJobUUID = secHubJobUUID;
        this.projectId = projectId;
    }

    public UUID getSecHubJobUUID() {
        return secHubJobUUID;
    }

    public String getResult() {
        return result;
    }

    public ScanReportResultType getResultType() {
        return resultType;
    }

    public void setResultType(ScanReportResultType resultType) {
        this.resultType = resultType;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setTrafficLight(TrafficLight trafficLight) {
        this.trafficLight = trafficLight;
    }

    public String getTrafficLightAsString() {
        if (trafficLight == null) {
            return null;
        }
        return trafficLight.name();
    }

    public UUID getUUID() {
        return uUID;
    }

    public void setResult(String result) {
        this.result = result;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uUID == null) ? 0 : uUID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScanReport other = (ScanReport) obj;
        if (uUID == null) {
            if (other.uUID != null)
                return false;
        } else if (!uUID.equals(other.uUID))
            return false;
        return true;
    }
}
