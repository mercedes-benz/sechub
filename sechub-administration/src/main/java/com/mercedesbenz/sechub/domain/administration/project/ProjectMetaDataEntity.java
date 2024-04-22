// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@IdClass(ProjectMetaDataEntityId.class)
@Table(name = ProjectMetaDataEntity.TABLE_NAME)
public class ProjectMetaDataEntity implements Serializable {

    private static final long serialVersionUID = -4875263434537107537L;

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "ADM_PROJECT_METADATA";

    public static final String COLUMN_PROJECT_ID = "PROJECT_ID";
    public static final String COLUMN_METADATA_KEY = "METADATA_KEY";
    public static final String COLUMN_METADATA_VALUE = "METADATA_VALUE";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = ProjectMetaDataEntity.class.getSimpleName();

    public static final String PROPERTY_PROJECT_ID = "projectId";
    public static final String PROPERTY_KEY = "key";
    public static final String PROPERTY_VALUE = "value";

    ProjectMetaDataEntity() {
        // jpa only
    }

    public ProjectMetaDataEntity(String projectId, String key, String value) {
        this.projectId = projectId;
        this.key = key;
        this.value = value;
    }

    @Id
    @Column(name = COLUMN_PROJECT_ID)
    String projectId;

    @Id
    @Column(name = COLUMN_METADATA_KEY)
    String key;

    @Column(name = COLUMN_METADATA_VALUE)
    String value;

    @Version
    @Column(name = "VERSION")
    Integer version;

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

        ProjectMetaDataEntity other = (ProjectMetaDataEntity) obj;

        return other.projectId.equals(projectId) && other.key.equals(key) && other.value.equals(value);
    }

    @Override
    public String toString() {
        return "ProjectMetaDataEntity [projectId=" + projectId + ", key=" + key + ", value=" + value + ", version=" + version + "]";
    }
}
