// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.whitelist;

import java.io.Serializable;
import java.net.URI;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * This entity represents only the whitelist uri entries for a project
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = ProjectWhitelistEntry.TABLE_NAME)
public class ProjectWhitelistEntry {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "SCHEDULE_PROJECT_WHITELIST";

    public static final String COLUMN_PROJECT_ID = "PROJECT_ID";
    public static final String COLUMN_WHITELIST_URI = "URI";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = ProjectWhitelistEntry.class.getSimpleName();

    @EmbeddedId
    private ProjectWhiteListEntryCompositeKey key;

    @Version
    @Column(name = "VERSION")
    Integer version;

    ProjectWhitelistEntry() {
        // jpa only
    }

    public ProjectWhitelistEntry(String projectId, URI uri) {
        this(new ProjectWhiteListEntryCompositeKey(projectId, uri));
    }

    public ProjectWhitelistEntry(ProjectWhiteListEntryCompositeKey key) {
        if (key == null) {
            throw new IllegalArgumentException("key may not be null");
        }
        if (key.projectId == null) {
            throw new IllegalArgumentException("key.projectId may not be null");
        }
        this.key = key;
    }

    public ProjectWhiteListEntryCompositeKey getKey() {
        return key;
    }

    @Embeddable
    public static class ProjectWhiteListEntryCompositeKey implements Serializable {

        private static final long serialVersionUID = 8753389792382752253L;

        @Column(name = COLUMN_PROJECT_ID, nullable = false)
        private String projectId;

        @Column(name = COLUMN_WHITELIST_URI, nullable = false)
        private URI uri;

        ProjectWhiteListEntryCompositeKey() {
            // jpa only
        }

        public ProjectWhiteListEntryCompositeKey(String projectId, URI uri) {
            this.uri = uri;
            this.projectId = projectId;
        }

        public URI getUri() {
            return uri;
        }

        public void setUri(URI uri) {
            this.uri = uri;
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
            result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
            ProjectWhiteListEntryCompositeKey other = (ProjectWhiteListEntryCompositeKey) obj;
            if (projectId == null) {
                if (other.projectId != null)
                    return false;
            } else if (!projectId.equals(other.projectId))
                return false;
            if (uri == null) {
                if (other.uri != null)
                    return false;
            } else if (!uri.equals(other.uri))
                return false;
            return true;
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
        ProjectWhitelistEntry other = (ProjectWhitelistEntry) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key)) {
            return false;
        }
        return true;
    }

}
