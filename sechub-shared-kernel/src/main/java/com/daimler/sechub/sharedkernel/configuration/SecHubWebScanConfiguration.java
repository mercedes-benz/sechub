// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.configuration;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class SecHubWebScanConfiguration {
	
	public static final String PROPERTY_URIS="uris";

	private List<URI> uris= new ArrayList<>();

	public List<URI> getUris() {
		return uris;
	}

}
