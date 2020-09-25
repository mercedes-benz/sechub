// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.daimler.sechub.commons.model.JSONable;
import com.daimler.sechub.sharedkernel.MustBeKeptStable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Input class for all kind of project data unknown values will be ignored.
 * Validators will handle different situations autark. So this class can be used
 * for multiple issues
 *
 * @author Albert Tregnaghi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@MustBeKeptStable("This class is used for creating projects etc. It is the value object for rest calls")
public class ProjectJsonInput implements JSONable<ProjectJsonInput> {

	public static final String PROPERTY_API_VERSION = "apiVersion";
	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_DESCRIPTION = "description";
	public static final String PROPERTY_WHITELIST = "whiteList";
	public static final String PROPERTY_OWNER = "owner";

	private String apiVersion;
	private String name;
	private String description;
	private String owner;

	private Optional<ProjectWhiteList> whiteList = Optional.empty();

	@Override
	public Class<ProjectJsonInput> getJSONTargetClass() {
		return ProjectJsonInput.class;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getOwner() {
		return owner;
	}

	public Optional<ProjectWhiteList> getWhiteList() {
		return whiteList;
	}

	public void setWhiteList(Optional<ProjectWhiteList> whiteList) {
		this.whiteList = whiteList;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static class ProjectWhiteList {

		public static final String PROPERTY_URIS = "uris";
		private List<URI> uris = new ArrayList<>();

		public List<URI> getUris() {
			return uris;
		}

	}
}
