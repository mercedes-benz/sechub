// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

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

import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a product executor configuration
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = ProductExecutorConfig.TABLE_NAME)
public class ProductExecutorConfig {

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
    
    public static final String COLUMN_PROFILES= "PROFILES_PROFILE_ID";
    

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = ProductExecutorConfig.class.getSimpleName();

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = COLUMN_UUID, updatable = false, nullable = false)
    @JsonProperty("uuid")
    UUID uUID;

    @Column(name = COLUMN_NAME)
    private String name;
    /**
     * The type of the product. Just to identify result content
     */
    @Enumerated(STRING)
    @Column(name = COLUMN_PRODUCT_IDENTIFIER, nullable = false)
    private ProductIdentifier productIdentifier;

    @Column(name = COLUMN_SETUP,columnDefinition = "text")
    @Convert(converter = ProductExecutorConfigSetupJpaConverter.class)
    @Basic(fetch = FetchType.EAGER)
    private ProductExecutorConfigSetup setup;

    @Version
    @Column(name = "VERSION")
    @JsonIgnore
    Integer version;

    @Column(name = COLUMN_EXECUTOR_VERSION)
    Integer executorVersion;

    @Column(name = COLUMN_ENABLED)
    Boolean enabled;
    
    @Column(name = COLUMN_PROFILES, nullable = false)
    @ManyToMany(cascade=CascadeType.REFRESH, mappedBy=ProductExecutionProfile.PROPERTY_CONFIGURATIONS, fetch=FetchType.EAGER)
    Set<ProductExecutionProfile> profiles = new HashSet<>();


    ProductExecutorConfig() {
        // jpa only
    }

    public ProductExecutorConfig(ProductIdentifier productIdentifier, ProductExecutorConfigSetup setup) {
        if (productIdentifier == null) {
            throw new IllegalArgumentException("Product identifier not be null!");
        }
        this.productIdentifier = productIdentifier;
        this.setup = setup;

    }

    public UUID getUUID() {
        return uUID;
    }

    public ProductIdentifier getProductIdentifier() {
        return productIdentifier;
    }

    public void setProductIdentifier(ProductIdentifier productIdentifier) {
        this.productIdentifier = productIdentifier;
    }

    public void setExecutorVersion(Integer executorVersion) {
        this.executorVersion = executorVersion;
    }

    public Integer getExecutorVersion() {
        return executorVersion;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setSetup(ProductExecutorConfigSetup setup) {
        this.setup = setup;
    }

    public ProductExecutorConfigSetup getSetup() {
        return setup;
    }

    public void setName(String name) {
        this.name = name;
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

}
