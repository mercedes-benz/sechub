// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

public interface ProjectRepositoryCustom {

    public void deleteProjectWithAssociations(String projectId);
}
