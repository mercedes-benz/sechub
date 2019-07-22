// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import java.net.URI;
import java.util.Optional;

import com.daimler.sechub.domain.administration.project.ProjectJsonInput.ProjectWhiteList;

public class ProjectJsonInputTestMain {
	public static void main(String[] args) {
		ProjectJsonInput input = new ProjectJsonInput();
		input.setApiVersion("1.0");
		input.setDescription("description");
		ProjectWhiteList list = new ProjectWhiteList();
		list.getUris().add(URI.create("https://localhost/test"));
		Optional<ProjectWhiteList> whitelist = Optional.of(list);
		input.setWhiteList(whitelist);
	
		System.out.println(input.toJSON());
	}
}
