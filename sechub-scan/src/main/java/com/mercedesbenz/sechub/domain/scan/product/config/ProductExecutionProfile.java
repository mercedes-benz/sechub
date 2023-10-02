// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.config;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Represents a product execution profile
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = ProductExecutionProfile.TABLE_NAME)
@JsonInclude(Include.NON_NULL)
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

    public static final String PROFILE_TO_PROJECT__COLUMN_PROJECT_ID = "PROJECTS_PROJECT_ID";
    public static final String PROFILE_TO_PROJECT__COLUMN_PROFILE_ID = "PRODUCT_EXECUTION_PROFILE_PROFILE_ID";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = "ProductExecutionProfile";

    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_CONFIGURATIONS = "configurations";
    public static final String PROPERTY_PROJECT_IDS = "projectIds";
    public static final String PROPERTY_ENABLED = "enabled";
    public static final String PROPERTY_DESCRIPTION = "description";

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

    @ManyToMany(cascade = { CascadeType.REFRESH }, fetch = FetchType.EAGER)
    @JoinTable(name = TABLE_NAME_PROFILE_TO_CONFIG)
    Set<ProductExecutorConfig> configurations = new HashSet<>();

    @Column(name = PROFILE_TO_PROJECT__COLUMN_PROJECT_ID, nullable = false)
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
