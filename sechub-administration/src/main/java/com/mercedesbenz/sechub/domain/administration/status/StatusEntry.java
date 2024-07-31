// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = StatusEntry.TABLE_NAME)
public class StatusEntry {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "ADM_STATUS";

    public static final String COLUMN_KEY_ID = "STATUS_ID";

    public static final String COLUMN_VALUE = "STATUS_VALUE";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = StatusEntry.class.getSimpleName();

    public static final String PROPERTY_KEY = "key";
    public static final String PROPERTY_VALUE = "value";

    @Id
    @Column(name = COLUMN_KEY_ID, unique = true, nullable = false)
    String key;

    @Column(name = COLUMN_VALUE, unique = true, nullable = false)
    String value;

    @Version
    @Column(name = "VERSION")
    Integer version;

    /* JPA only */
    StatusEntry() {

    }

    public StatusEntry(StatusEntryKey key) {
        this.key = key.getStatusEntryKey();
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
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
        StatusEntry other = (StatusEntry) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key)) {
            return false;
        }
        return true;
    }

}