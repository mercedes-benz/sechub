// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.configuration;

import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class SecHubInfrastructureScanConfiguration {

	public static final String PROPERTY_URIS="uris";
	public static final String PROPERTY_IPS="ips";
	
	private List<URI> uris= new ArrayList<>();
	private List<InetAddress> ips= new ArrayList<>();

	public List<URI> getUris() {
		return uris;
	}
	
	public List<InetAddress> getIps() {
		return ips;
	}


}
