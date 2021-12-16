// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.model;

import java.net.URI;
import java.util.Optional;

import com.daimler.sechub.commons.model.login.WebLoginConfiguration;

public class SecHubWebScanConfiguration {

	public static final String PROPERTY_URI = "uri";
	public static final String PROPERTY_LOGIN = "login";
	public static final String PROPERTY_MAX_SCAN_DURATION = "maxScanDuration";

	Optional<WebLoginConfiguration> login = Optional.empty();
	Optional<WebScanDurationConfiguration> maxScanDuration = Optional.empty();

	URI uri;
	
	public URI getUri() {
	    return uri;
	}

	public Optional<WebLoginConfiguration> getLogin() {
		return login;
	}
	
	public Optional<WebScanDurationConfiguration> getMaxScanDuration() {
	    return maxScanDuration;
	}
}
