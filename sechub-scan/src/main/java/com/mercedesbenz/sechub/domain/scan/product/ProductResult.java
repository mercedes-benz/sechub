// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import static jakarta.persistence.EnumType.*;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;

import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigInfo;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Represents a product result for a SecHub job UUID
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = ProductResult.TABLE_NAME)
public class ProductResult {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "SCAN_PRODUCT_RESULT";

    public static final String COLUMN_UUID = "UUID";
    public static final String COLUMN_SECHUB_JOB_UUID = "SECHUB_JOB_UUID";
    public static final String COLUMN_PRODUCT_IDENTIFIER = "PRODUCT_ID";

    public static final String COLUMN_RESULT = "RESULT";

    public static final String COLUMN_PROJECT_ID = "PROJECT_ID";

    public static final String COLUMN_STARTED = "STARTED";
    public static final String COLUMN_ENDED = "ENDED";
    public static final String COLUMN_META_DATA = "META_DATA";

    public static final String COLUMN_PRODUCT_CONFIG_UUID = "PRODUCT_CONFIG_UUID";

    public static final String COLUMN_MESSAGES = "MESSAGES";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = ProductResult.class.getSimpleName();

    public static final String PROPERTY_SECHUB_JOB_UUID = "secHubJobUUID";
    public static final String PROPERTY_PRODUCT_IDENTIFIER = "productIdentifier";
    public static final String PROPERTY_PRODUCT_CONFIG_UUID = "productExecutorConfigUUID";
    public static final String PROPERTY_PRODUCT_STARTED = "started";
    public static final String PROPERTY_MESSAGES = "messages";

    public static final String QUERY_DELETE_RESULT_OLDER_THAN = "DELETE FROM ProductResult r WHERE r." + PROPERTY_PRODUCT_STARTED + " < :cleanTimeStamp";;

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = COLUMN_UUID, updatable = false, nullable = false, columnDefinition = "UUID")
    UUID uUID;

    public UUID getUUID() {
        return uUID;
    }

    /**
     * The type of the product. Just to identify result content
     */
    @Enumerated(STRING)
    @Column(name = COLUMN_PRODUCT_IDENTIFIER, nullable = false)
    ProductIdentifier productIdentifier;

    @JdbcTypeCode(Types.LONGVARCHAR) // why not using @Lob, because hibernate/postgres issues. see
                                     // https://stackoverflow.com/questions/25094410/hibernate-error-while-persisting-text-datatype?noredirect=1#comment39048566_25094410
                                     // In Hibernate 6: https://stackoverflow.com/a/74602072
    @Column(name = COLUMN_RESULT)
    private String result;

    @Column(name = COLUMN_SECHUB_JOB_UUID, updatable = false, nullable = false, columnDefinition = "UUID")
    UUID secHubJobUUID;

    @Version
    @Column(name = "VERSION")
    Integer version;

    @Column(name = COLUMN_PROJECT_ID, nullable = false)
    String projectId;

    @Column(name = COLUMN_STARTED) // remark: we setup hibernate to use UTC settings - see application.properties
    LocalDateTime started;

    @Column(name = COLUMN_ENDED) // remark: we setup hibernate to use UTC settings - see application.properties
    LocalDateTime ended;

    @JdbcTypeCode(Types.LONGVARCHAR)
    @Column(name = COLUMN_META_DATA, nullable = true)
    String metaData;

    @Column(name = COLUMN_PRODUCT_CONFIG_UUID, nullable = true, columnDefinition = "UUID") // when null it means we got (old) entries or SERECO fallback
    UUID productExecutorConfigUUID;

    @JdbcTypeCode(Types.LONGVARCHAR)
    @Column(name = COLUMN_MESSAGES, nullable = true)
    String messages;

    ProductResult() {
        // jpa only
    }

    /**
     * Create the product result
     *
     * @param secHubJobUUID
     * @param projectId
     * @param productIdentifier
     * @param result            as string
     */
    public ProductResult(UUID secHubJobUUID, String projectId, ProductExecutorConfigInfo productExecutorInfo, String result) {
        if (secHubJobUUID == null) {
            throw new IllegalArgumentException("SecHub JOB UUID may not be null!");
        }
        if (productExecutorInfo == null) {
            throw new IllegalArgumentException("Product executor config info may not be null!");
        }
        this.productIdentifier = productExecutorInfo.getProductIdentifier();

        if (productIdentifier == null) {
            throw new IllegalArgumentException("Product identifier not be null!");
        }
        this.productExecutorConfigUUID = productExecutorInfo.getUUID();

        this.secHubJobUUID = secHubJobUUID;
        this.projectId = projectId;
        this.result = result;

    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getProjectId() {
        return projectId;
    }

    public ProductIdentifier getProductIdentifier() {
        return productIdentifier;
    }

    public UUID getSecHubJobUUID() {
        return secHubJobUUID;
    }

    public String getResult() {
        return result;
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

    public void setMetaData(String metatData) {
        this.metaData = metatData;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setProductExecutorConfigUUID(UUID productExecutorConfigUUID) {
        this.productExecutorConfigUUID = productExecutorConfigUUID;
    }

    public UUID getProductExecutorConfigUUID() {
        return productExecutorConfigUUID;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    public String getMessages() {
        return messages;
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
        ProductResult other = (ProductResult) obj;
        if (uUID == null) {
            if (other.uUID != null)
                return false;
        } else if (!uUID.equals(other.uUID))
            return false;
        return true;
    }

}
