// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

import com.daimler.sechub.sharedkernel.MustBeKeptStable;
import com.daimler.sechub.sharedkernel.util.JSONable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This message data object contains all possible information about a project
 * which can be interesting for messaging. BUT: It dependes on the
 * {@link MessageID} which parts are set.
 *
 * @author Albert Tregnaghi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@MustBeKeptStable("This configuration is used by communication between (api) schedule domain and administration - and maybe others")
public class ProjectMessage implements JSONable<ProjectMessage> {

	private Set<URI> whitelist = new LinkedHashSet<>();

	private String projectId;

	@Override
	public Class<ProjectMessage> getJSONTargetClass() {
		return ProjectMessage.class;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	public String getProjectId() {
		return projectId;
	}

	public void setWhitelist(Set<URI> whitelist) {
		this.whitelist = whitelist;
	}

	public Set<URI> getWhitelist() {
		return whitelist;
	}

}
