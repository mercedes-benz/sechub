// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import java.io.Serializable;
import java.util.Objects;

public class ProjectMetaDataEntityId implements Serializable {
	
	private static final long serialVersionUID = 4251170992198601592L;
	
	String projectId;
	String key;
	
    @Override
    public int hashCode() {
        return Objects.hash(key, projectId);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProjectMetaDataEntityId other = (ProjectMetaDataEntityId) obj;
        return Objects.equals(key, other.key) && Objects.equals(projectId, other.projectId);
    }

}
