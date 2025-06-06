// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.sharedkernel.project.ProjectAccessLevel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = Project.TABLE_NAME)
public class Project {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "ADM_PROJECT";
    public static final String TABLE_NAME_PROJECT_TO_USER = "ADM_PROJECT_TO_USER";
    public static final String TABLE_NAME_PROJECT_TO_METADATA = "ADM_PROJECT_TO_METADATA";
    public static final String TABLE_NAME_PROJECT_WHITELIST_URI = "ADM_PROJECT_WHITELIST_URI";
    public static final String TABLE_NAME_PROJECT_METADATA = "ADM_PROJECT_METADATA";
    public static final String TABLE_NAME_PROJECT_TEMPLATES = "ADM_PROJECT_TEMPLATES";

    public static final String COLUMN_PROJECT_ID = "PROJECT_ID";
    public static final String COLUMN_PROJECT_OWNER = "PROJECT_OWNER";
    public static final String COLUMN_PROJECT_DESCRIPTION = "PROJECT_DESCRIPTION";
    public static final String COLUMN_WHITELIST_URIS = "PROJECT_WHITELIST_URIS";
    public static final String COLUMN_METADATA = "METADATA_KEY";

    public static final String COLUMN_PROJECT_ACCESS_LEVEL = "PROJECT_ACCESS_LEVEL";
    public static final String COLUMN_TEMPLATE_ID = "PROJECT_TEMPLATE_ID";

    public static final String ASSOCIATE_PROJECT_TO_USER_COLUMN_PROJECT_ID = "PROJECTS_PROJECT_ID";
    public static final String ASSOCIATE_PROJECT_TO_USER_COLUMN_USER_ID = "USERS_USER_ID";
    public static final String ASSOCIATE_PROJECT_TO_URI_COLUMN_PROJECT_ID = "PROJECT_PROJECT_ID";
    public static final String ASSOCIATE_PROJECT_TO_METADATA_COLUMN_PROJECT_ID = "PROJECT_ID";
    public static final String ASSOCIATE_PROJECT_TO_TEMPLATE_COLUMN_PROJECT_ID = "PROJECT_PROJECT_ID";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = "Project";

    public static final String PROPERTY_USERS = "users";
    public static final String PROPERTY_OWNER = "owner";
    public static final String PROPERTY_ID = "id";

    @Id
    @Column(name = COLUMN_PROJECT_ID)
    String id;

    @Column(name = COLUMN_PROJECT_DESCRIPTION)
    String description;

    @Column(name = COLUMN_PROJECT_ACCESS_LEVEL, nullable = false)
    ProjectAccessLevel accessLevel = ProjectAccessLevel.FULL;

    // no merge cascade or persist, because owner and user in set
    // otherwise leading to "java.lang.IllegalStateException: Multiple
    // representations of the same entity ... are being merged"
    @ManyToMany(cascade = { CascadeType.REFRESH }, fetch = FetchType.EAGER)
    @JoinTable(name = TABLE_NAME_PROJECT_TO_USER, joinColumns = @JoinColumn(name = ASSOCIATE_PROJECT_TO_USER_COLUMN_PROJECT_ID), inverseJoinColumns = @JoinColumn(name = ASSOCIATE_PROJECT_TO_USER_COLUMN_USER_ID))
    Set<User> users = new HashSet<>();

    // we do not CascadeType.Persist or ALL because otherwise user will be persisted
    // again and leads to unique constraint violation
    @ManyToOne(cascade = { CascadeType.REFRESH }, fetch = FetchType.EAGER)
    @JoinColumn(name = COLUMN_PROJECT_OWNER, nullable = false)
    User owner;

    @Column(name = COLUMN_WHITELIST_URIS, nullable = false)
    @ElementCollection(targetClass = URI.class, fetch = FetchType.EAGER)
    @CollectionTable(name = TABLE_NAME_PROJECT_WHITELIST_URI)
    Set<URI> whiteList = new HashSet<>();

    @OneToMany(cascade = { CascadeType.REFRESH }, fetch = FetchType.EAGER, mappedBy = ProjectMetaDataEntity.PROPERTY_PROJECT_ID)
    Set<ProjectMetaDataEntity> metaData = new HashSet<>();

    @Column(name = COLUMN_TEMPLATE_ID, nullable = false)
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = TABLE_NAME_PROJECT_TEMPLATES)
    Set<String> templateIds = new HashSet<>();

    @Version
    @Column(name = "VERSION")
    Integer version;

    /**
     * Returns white list entries. Why URIs and not URIs and IPs? Because an IP can
     * be contained as LITERALS inside a URI (v4 and v6)- see {@link URI} for
     * details
     *
     * @return a set with white lists.
     */
    public Set<URI> getWhiteList() {
        return whiteList;
    }

    public Set<ProjectMetaDataEntity> getMetaData() {
        return metaData;
    }

    public Set<User> getUsers() {
        return users;
    }

    public User getOwner() {
        return owner;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public ProjectAccessLevel getAccessLevel() {
        return accessLevel;
    }

    public Set<String> getTemplateIds() {
        return templateIds;
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
        Project other = (Project) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

}