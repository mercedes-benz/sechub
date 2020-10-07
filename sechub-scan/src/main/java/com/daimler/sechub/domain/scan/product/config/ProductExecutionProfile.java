// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * Represents a product execution profile
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = ProductExecutionProfile.TABLE_NAME)
public class ProductExecutionProfile {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "SCAN_PRODUCT_EXECUTION_PROFILE";
    public static final String TABLE_NAME_PROFILE_TO_CONFIG = "SCAN_EXECUTION_PROFILE_TO_CONFIG";
    public static final String TABLE_NAME_PROFILE_TO_PROJECT = "SCAN_EXECUTION_PROFILE_TO_PROJECT";
    
    public static final String COLUMN_PROFILE_ID = "PROFILE_ID";
    public static final String COLUMN_PROFILE_DESCRIPTION = "PROFILE_DESCRIPTION";
    public static final String COLUMN_PROFILE_ENABLED = "PROFILE_ENABLED";
    public static final String COLUMN_PROJECT_IDS= "PROJECTS_PROJECT_ID";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = ProductExecutionProfile.class.getSimpleName();

    public static final String PROPERTY_ID = "id";
    public static final String ASSOCIATE_PROFILE_TO_CONFIG_ID = "PROFILES_PROFILE_ID";
    public static final String ASSOCIATE_PROFILE_TO_PROJECT_ID = "PROJECTS_PROJECT_ID";
    public static final String PROPERTY_CONFIGURATIONS = "configurations";
    public static final String PROPERTY_PROJECT_IDS = "projectIds";
    public static final String PROPERTY_ENABLED = "enabled";

    @Id
    @Column(name = COLUMN_PROFILE_ID)
    String id;

    @Column(name = COLUMN_PROFILE_DESCRIPTION)
    String description;
    
    @Column(name = COLUMN_PROFILE_ENABLED)
    Boolean enabled;
    
    @Version
    @Column(name = "VERSION")
    Integer version;
    
    @ManyToMany(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinTable(name = TABLE_NAME_PROFILE_TO_CONFIG)
    Set<ProductExecutorConfig> configurations = new HashSet<>();
    
    @Column(name = COLUMN_PROJECT_IDS, nullable = false)
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = TABLE_NAME_PROFILE_TO_PROJECT)
    Set<String> projectIds = new HashSet<>();
    
    public Set<ProductExecutorConfig> getConfigurations() {
        return configurations;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public Set<String> getProjectIds() {
        return projectIds;
    }
    
    public String getId() {
        return id;
    }
    
    public String getDescription() {
        return description;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProductExecutionProfile other = (ProductExecutionProfile) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public String toString() {
        return "ProductExecutionProfile [" + (id != null ? "id=" + id + ", " : "") + (enabled != null ? "enabled=" + enabled : "") + "]";
    }
    
}
