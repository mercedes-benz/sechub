// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
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
		
		Map<String, String> metaData = new HashMap<>();
		metaData.put("key1", "value1");
		input.setMetaData(Optional.of(metaData));
	
		System.out.println(input.toJSON());
	}
}
