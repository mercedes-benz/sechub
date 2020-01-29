// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.configuration;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.daimler.sechub.sharedkernel.configuration.login.WebLoginConfiguration;

public class SecHubWebScanConfiguration {

	public static final String PROPERTY_URIS = "uris";
	public static final String PROPERTY_LOGIN = "login";

	Optional<WebLoginConfiguration> login = Optional.empty();
	private List<URI> uris = new ArrayList<>();


	public List<URI> getUris() {
		return uris;
	}

	public Optional<WebLoginConfiguration> getLogin() {
		return login;
	}

}
