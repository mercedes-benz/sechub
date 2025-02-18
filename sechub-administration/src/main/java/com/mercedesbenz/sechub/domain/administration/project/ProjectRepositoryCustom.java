// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

public interface ProjectRepositoryCustom {

    public void deleteProjectWithAssociations(String projectId);

    /**
     * Any assignment with given template id will be removed
     *
     * @param templateId template identifier to remove from projects
     */
    public void deleteTemplateAssignmentFromAnyProject(String templateId);
}
