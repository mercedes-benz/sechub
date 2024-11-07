// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.asset;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Represents a template
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = AssetFile.TABLE_NAME)
public class AssetFile {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "SCAN_ASSET_FILE";

    public static final String COLUMN_ASSET_ID = "ASSET_ID";
    public static final String COLUMN_FILE_NAME = "FILE_NAME";
    public static final String COLUMN_DATA = "DATA";
    public static final String COLUMN_CHECKSUM = "CHECKSUM";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = "AssetFile";

    @EmbeddedId
    AssetFileCompositeKey key;

    @Column(name = COLUMN_CHECKSUM)
    String checksum;

    @Column(name = COLUMN_DATA)
    byte[] data;

    @Version
    @Column(name = "VERSION")
    @JsonIgnore
    Integer version;

    AssetFile() {
        // jpa only
    }

    public AssetFile(AssetFileCompositeKey key) {
        this.key = key;
    }

    public AssetFileCompositeKey getKey() {
        return key;
    }

    public void setChecksum(String definition) {
        this.checksum = definition;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    /**
     * Asset id and file name or only strings. To avoid confusion in constructor
     * usage, this builder was introduced.
     *
     * @author Albert Tregnaghi
     *
     */
    public static class AssetFileCompositeKeyBuilder {

        private String assetId;
        private String fileName;

        public AssetFileCompositeKey build() {

            if (assetId == null) {
                throw new IllegalStateException("asset id not defined!");
            }
            if (fileName == null) {
                throw new IllegalStateException("file name not defined!");
            }

            AssetFileCompositeKey key = new AssetFileCompositeKey();
            key.setAssetId(assetId);
            key.setFileName(fileName);

            return key;
        }

        public AssetFileCompositeKeyBuilder assetId(String assetId) {
            this.assetId = assetId;
            return this;
        }

        public AssetFileCompositeKeyBuilder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }
    }

    @Embeddable
    public static class AssetFileCompositeKey implements Serializable {

        public static AssetFileCompositeKeyBuilder builder() {
            return new AssetFileCompositeKeyBuilder();
        }

        private static final long serialVersionUID = 8753389792382752253L;

        @Column(name = COLUMN_ASSET_ID, nullable = false)
        private String assetId;

        @Column(name = COLUMN_FILE_NAME, nullable = false)
        private String fileName;

        AssetFileCompositeKey() {
            // jpa only
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String userId) {
            this.fileName = userId;
        }

        public String getAssetId() {
            return assetId;
        }

        public void setAssetId(String projectId) {
            this.assetId = projectId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((assetId == null) ? 0 : assetId.hashCode());
            result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
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
            AssetFileCompositeKey other = (AssetFileCompositeKey) obj;
            if (assetId == null) {
                if (other.assetId != null)
                    return false;
            } else if (!assetId.equals(other.assetId))
                return false;
            if (fileName == null) {
                if (other.fileName != null)
                    return false;
            } else if (!fileName.equals(other.fileName))
                return false;
            return true;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AssetFile other = (AssetFile) obj;
        return Objects.equals(key, other.key);
    }

    @Override
    public String toString() {
        return "AssetFile [" + (key != null ? "key=" + key + ", " : "") + (checksum != null ? "checksum=" + checksum + ", " : "")
                + (version != null ? "version=" + version : "") + "]";
    }
}
