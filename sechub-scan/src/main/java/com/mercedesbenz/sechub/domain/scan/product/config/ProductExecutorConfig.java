// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.config;

import static javax.persistence.EnumType.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mercedesbenz.sechub.domain.scan.product.ProductIdentifier;

/**
 * Represents a product executor configuration
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = ProductExecutorConfig.TABLE_NAME)
public class ProductExecutorConfig implements ProductExecutorConfigInfo {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "SCAN_PRODUCT_EXECUTOR_CONFIG";

    public static final String COLUMN_UUID = "CONFIG_UUID";
    public static final String COLUMN_NAME = "CONFIG_NAME";
    public static final String COLUMN_EXECUTOR_VERSION = "CONFIG_EXECUTOR_VERSION";
    public static final String COLUMN_PRODUCT_IDENTIFIER = "CONFIG_PRODUCT_ID";
    public static final String COLUMN_SETUP = "CONFIG_SETUP";
    public static final String COLUMN_ENABLED = "CONFIG_ENABLED";
    public static final String COLUMN_PROFILES = "PROFILES_PROFILE_ID";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = "ProductExecutorConfig";

    public static final String PROPERTY_UUID = "uuid";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_PRODUCTIDENTIFIER = "productIdentifier";
    public static final String PROPERTY_SETUP = "setup";
    public static final String PROPERTY_EXECUTORVERSION = "executorVersion";
    public static final String PROPERTY_ENABLED = "enabled";
    public static final String PROPERTY_PROFILES = "profiles";

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = COLUMN_UUID, updatable = false, nullable = false)
    @JsonProperty("uuid")
    UUID uUID;

    @Column(name = COLUMN_NAME)
    String name;
    /**
     * The type of the product. Just to identify result content
     */
    @Enumerated(STRING)
    @Column(name = COLUMN_PRODUCT_IDENTIFIER, nullable = false)
    ProductIdentifier productIdentifier;

    @Column(name = COLUMN_SETUP, columnDefinition = "text")
    @Convert(converter = ProductExecutorConfigSetupJpaConverter.class)
    @Basic(fetch = FetchType.EAGER)
    ProductExecutorConfigSetup setup;

    @Version
    @Column(name = "VERSION")
    @JsonIgnore
    Integer version;

    @Column(name = COLUMN_EXECUTOR_VERSION)
    Integer executorVersion;

    @Column(name = COLUMN_ENABLED)
    Boolean enabled;

    @Column(name = COLUMN_PROFILES, nullable = false)
    @ManyToMany(cascade = CascadeType.REFRESH, mappedBy = ProductExecutionProfile.PROPERTY_CONFIGURATIONS, fetch = FetchType.EAGER)
    Set<ProductExecutionProfile> profiles = new HashSet<>();

    ProductExecutorConfig() {
        // jpa only
    }

    public ProductExecutorConfig(ProductIdentifier productIdentifier, int executorVersion, ProductExecutorConfigSetup setup) {
        if (productIdentifier == null) {
            throw new IllegalArgumentException("Product identifier not be null!");
        }
        this.productIdentifier = productIdentifier;
        this.executorVersion = executorVersion;
        this.setup = setup;

    }

    public UUID getUUID() {
        return uUID;
    }

    public ProductIdentifier getProductIdentifier() {
        return productIdentifier;
    }

    public Integer getExecutorVersion() {
        return executorVersion;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public ProductExecutorConfigSetup getSetup() {
        return setup;
    }

    public String getName() {
        return name;
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
        ProductExecutorConfig other = (ProductExecutorConfig) obj;
        if (uUID == null) {
            if (other.uUID != null)
                return false;
        } else if (!uUID.equals(other.uUID))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ProductExecutorConfig [" + (uUID != null ? "uUID=" + uUID + ", " : "") + (name != null ? "name=" + name + ", " : "")
                + (productIdentifier != null ? "productIdentifier=" + productIdentifier + ", " : "")
                + (executorVersion != null ? "executorVersion=" + executorVersion + ", " : "") + (enabled != null ? "enabled=" + enabled : "") + "]";
    }

}
