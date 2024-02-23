// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.config;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Global configuration entry for domain 'schedule' inside database. Contains
 * only ONE row! {@link #ID}
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = SchedulerConfig.TABLE_NAME)
public class SchedulerConfig {

    /**
     * We got only ONE schedule configuration entry inside table. So we use always
     * only the first one here!
     */
    public static final Integer ID = Integer.valueOf(0);

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "SCHEDULE_CONFIG";

    public static final String COLUMN_ID = "CONFIG_ID";

    public static final String COLUMN_JOB_PROCESSING_ENABLED = "CONFIG_JOB_PROCESSING_ENABLED";

    public static final String COLUMN_AUTO_CLEANUP_IN_DAYS = "CONFIG_AUTO_CLEANUP_IN_DAYS";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = SchedulerConfig.class.getSimpleName();

    @Id
    @Column(name = COLUMN_ID, unique = true, nullable = false)
    Integer id = ID;

    @Column(name = COLUMN_JOB_PROCESSING_ENABLED, nullable = false)
    boolean jobProcessingEnabled = true;

    @Column(name = COLUMN_AUTO_CLEANUP_IN_DAYS, nullable = false)
    Long autoCleanupInDays = Long.valueOf(0); // per default 0 (avoid NPEs when auto casting)

    @Version
    @Column(name = "VERSION")
    Integer version;

    public void setJobProcessingEnabled(boolean jobProcessingEnabled) {
        this.jobProcessingEnabled = jobProcessingEnabled;
    }

    public boolean isJobProcessingEnabled() {
        return jobProcessingEnabled;
    }

    public Integer getId() {
        return id;
    }

    public Long getAutoCleanupInDays() {
        return autoCleanupInDays;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        SchedulerConfig other = (SchedulerConfig) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

}