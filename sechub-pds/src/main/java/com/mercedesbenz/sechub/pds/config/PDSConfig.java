// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.config;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * Global configuration entry for domain 'administration' inside database.
 * Contains only ONE row! see {@link #ID}
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = PDSConfig.TABLE_NAME)
public class PDSConfig {

    /**
     * We got only ONE administration configuration entry inside table. So we use
     * always only the first one here!
     */
    public static final Integer ID = Integer.valueOf(0);

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "PDS_CONFIG";

    public static final String COLUMN_ID = "CONFIG_ID";

    public static final String COLUMN_AUTO_CLEANUP_CONFIGURATION = "CONFIG_AUTO_CLEANUP";

    public static final String COLUMN_AUTO_CLEANUP_IN_DAYS = "CONFIG_AUTO_CLEANUP_IN_DAYS";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = PDSConfig.class.getSimpleName();

    @Id
    @Column(name = COLUMN_ID, unique = true, nullable = false)
    Integer id = ID;

    @Column(name = COLUMN_AUTO_CLEANUP_CONFIGURATION, nullable = false)
    String autoCleanupConfiguration;

    @Column(name = COLUMN_AUTO_CLEANUP_IN_DAYS, nullable = false)
    Long autoCleanupInDays = Long.valueOf(0); // per default 0 (avoid NPEs when auto casting)

    @Version
    @Column(name = "VERSION")
    Integer version;

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
        PDSConfig other = (PDSConfig) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

}