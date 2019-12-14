// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import com.daimler.sechub.domain.administration.user.User;

public class TestProjectCreationFactory {

	public static Project createProject(String projectId, User owner) {
		Project project= new Project();
		project.id=projectId;
		project.owner=owner;
		return project;
	}
}
