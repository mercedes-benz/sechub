// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import static jakarta.persistence.EnumType.*;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * PersistentCipher pool data table for domain 'schedule' inside database.
 * Contains information about how existing schedule job entries are encrypted.
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = ScheduleCipherPoolData.TABLE_NAME)
public class ScheduleCipherPoolData {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "SCHEDULE_CIPHER_POOL_DATA";

    public static final String COLUMN_ID = "POOL_ID";

    public static final String COLUMN_ALGORITHM = "POOL_ALGORITHM";

    public static final String COLUMN_PWD_SOURCE_TYPE = "POOL_PWD_SRC_TYPE";

    public static final String COLUMN_PWD_SOURCE_DATA = "POOL_PWD_SRC_DATA";

    public static final String COLUMN_TEST_TEXT = "POOL_TEST_TEXT";
    public static final String COLUMN_TEST_INITIAL_VECTOR = "POOL_TEST_INITIAL_VECTOR";
    public static final String COLUMN_TEST_ENCRYPTED = "POOL_TEST_ENCRYPTED";

    public static final String COLUMN_CREATION_TIMESTAMP = "POOL_CREATION_TIMESTAMP";
    public static final String COLUMN_CREATED_FROM = "POOL_CREATED_FROM";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = ScheduleCipherPoolData.class.getSimpleName();

    @Id
    @Column(name = COLUMN_ID, unique = true, nullable = false)
    Long id;

    @Enumerated(STRING)
    @Column(name = COLUMN_ALGORITHM, nullable = false)
    CipherAlgorithm algorithm;

    @Enumerated(STRING)
    @Column(name = COLUMN_PWD_SOURCE_TYPE, nullable = false)
    CipherPasswordSourceType cipherPasswordSourceType;

    @Column(name = COLUMN_PWD_SOURCE_DATA, nullable = true)
    String passwordSourceData;

    @Column(name = COLUMN_TEST_TEXT, nullable = false)
    String testText;

    @Column(name = COLUMN_TEST_INITIAL_VECTOR, nullable = true)
    byte[] testInitialVector;

    @Column(name = COLUMN_TEST_ENCRYPTED, nullable = false)
    byte[] testEncrypted;

    @Column(name = COLUMN_CREATION_TIMESTAMP, nullable = false) // remark: we setup hibernate to use UTC settings - see
    LocalDateTime created;

    @Column(name = COLUMN_CREATED_FROM, nullable = true)
    String createdFrom;

    @Version
    @Column(name = "VERSION")
    Integer version;

    public Long getId() {
        return id;
    }

    public CipherAlgorithm getAlgorithm() {
        return algorithm;
    }

    public CipherPasswordSourceType getPasswordSourceType() {
        return cipherPasswordSourceType;
    }

    public String getPasswordSourceData() {
        return passwordSourceData;
    }

    public String getTestText() {
        return testText;
    }

    public byte[] getTestInitialVector() {
        return testInitialVector;
    }

    public byte[] getTestEncrypted() {
        return testEncrypted;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public String getCreatedFrom() {
        return createdFrom;
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
        ScheduleCipherPoolData other = (ScheduleCipherPoolData) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

}