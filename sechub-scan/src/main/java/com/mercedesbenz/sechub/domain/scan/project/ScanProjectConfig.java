// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.notNull;

import java.io.Serializable;
import java.sql.Types;

import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Configuration entry for project configuration inside database (domain scan).
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = ScanProjectConfig.TABLE_NAME)
public class ScanProjectConfig {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "SCAN_PROJECT_CONFIG";

    public static final String COLUMN_PROJECT_ID = "PROJECT_ID";
    public static final String COLUMN_CONFIG_ID = "CONFIG_ID";

    public static final String COLUMN_DATA = "DATA";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = ScanProjectConfig.class.getSimpleName();

    public static final String PROPERTY_KEY = "key";
    public static final String PROPERTY_DATA = "data";

    public static final String QUERY_FIND_ALL_CONFIGURATIONS_FOR_PROJECT = "SELECT c FROM ScanProjectConfig c where c." + PROPERTY_KEY + "."
            + ScanProjectConfigCompositeKey.PROPERTY_PROJECT_ID + " =:projectId";

    public static final String QUERY_FIND_ALL_DATA_FOR_CONFIG_ID = "SELECT c." + PROPERTY_DATA + " FROM ScanProjectConfig c where c." + PROPERTY_KEY + "."
            + ScanProjectConfigCompositeKey.PROPERTY_CONFIG_ID + " =:configId";

    public static final String QUERY_FIND_ALL_PROJECT_IDS_FOR_SET_OF_CONFIG_IDS_AND_DATA = "SELECT c." + PROPERTY_KEY + "."
            + ScanProjectConfigCompositeKey.PROPERTY_PROJECT_ID + " FROM ScanProjectConfig c where c." + PROPERTY_KEY + "."
            + ScanProjectConfigCompositeKey.PROPERTY_CONFIG_ID + " in :configIds AND c." + PROPERTY_DATA + " =:data";

    @EmbeddedId
    ScanProjectConfigCompositeKey key;

    @Version
    @Column(name = "VERSION")
    Integer version;

    @JdbcTypeCode(Types.LONGVARCHAR)
    @Column(name = COLUMN_DATA)
    String data;

    ScanProjectConfig() {
        // jpa only
    }

    public ScanProjectConfig(ScanProjectConfigID configId, String projectId) {
        this(new ScanProjectConfigCompositeKey(configId, projectId));
    }

    public ScanProjectConfig(ScanProjectConfigCompositeKey key) {
        if (key == null) {
            throw new IllegalArgumentException("key may not be null");
        }
        if (key.projectId == null) {
            throw new IllegalArgumentException("key.projectId may not be null");
        }
        if (key.configId == null) {
            throw new IllegalArgumentException("key.configId may not be null");
        }
        this.key = key;
    }

    public ScanProjectConfigCompositeKey getKey() {
        return key;
    }

    /**
     * Set data as a string. Either use simple values or a JSON for structured data
     *
     * @param data
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     *
     * @return data as string. Either simple values or a JSON for structured data
     */
    public String getData() {
        return data;
    }

    @Embeddable
    public static class ScanProjectConfigCompositeKey implements Serializable {

        private static final long serialVersionUID = 8753389792382752253L;

        public static final String PROPERTY_PROJECT_ID = "projectId";
        public static final String PROPERTY_CONFIG_ID = "configId";

        @Column(name = COLUMN_PROJECT_ID, nullable = false)
        private String projectId;

        @Column(name = COLUMN_CONFIG_ID, nullable = false)
        private String configId;

        ScanProjectConfigCompositeKey() {
            // jpa only
        }

        public ScanProjectConfigCompositeKey(ScanProjectConfigID configId, String projectId) {
            this(nonNullConfigId(configId), projectId);
        }

        private static String nonNullConfigId(ScanProjectConfigID configId) {
            notNull(configId, "Config ID may not be null!");
            return configId.getId();
        }

        /* just internal and for tests */
        ScanProjectConfigCompositeKey(String configId, String projectId) {
            this.configId = configId;
            this.projectId = projectId;
        }

        public String getConfigId() {
            return configId;
        }

        public void setConfigId(String configId) {
            this.configId = configId;
        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((projectId == null) ? 0 : projectId.hashCode());
            result = prime * result + ((configId == null) ? 0 : configId.hashCode());
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
            ScanProjectConfigCompositeKey other = (ScanProjectConfigCompositeKey) obj;
            if (projectId == null) {
                if (other.projectId != null)
                    return false;
            } else if (!projectId.equals(other.projectId))
                return false;
            if (configId == null) {
                if (other.configId != null)
                    return false;
            } else if (!configId.equals(other.configId))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "ScanProjectConfigCompositeKey [" + (projectId != null ? "projectId=" + projectId + ", " : "")
                    + (configId != null ? "configId=" + configId : "") + "]";
        }
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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScanProjectConfig other = (ScanProjectConfig) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ScanProjectConfig [" + (key != null ? "key=" + key + ", " : "") + (data != null ? "data=" + data : "") + "]";
    }

}