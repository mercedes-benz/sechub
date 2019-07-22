// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import java.net.InetAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.crypto.SealedObject;

public abstract class AbstractAdapterConfig implements AdapterConfig {

	String productBaseURL;
	String base64Token;

	int timeToWaitForNextCheckOperationInMilliseconds;
	int timeOutInMilliseconds;
	int proxyPort;
	String proxyHostname;

	String user;
	SealedObject password;

	String policyId;

	String projectId;

	LinkedHashSet<URI> targetURIs=new LinkedHashSet<>();
	LinkedHashSet<URI> rootTargetUris = new LinkedHashSet<>();

	String traceID;
	boolean trustAllCertificatesEnabled;
	private Map<String, Object> options = new HashMap<>();
	LinkedHashSet<InetAddress> targetIPs = new LinkedHashSet<>();
	private String targetType;


	protected AbstractAdapterConfig() {
	}



	@Override
	public final int getTimeOutInMilliseconds() {
		return timeOutInMilliseconds;
	}

	@Override
	public int getTimeToWaitForNextCheckOperationInMilliseconds() {
		return timeToWaitForNextCheckOperationInMilliseconds;
	}

	@Override
	public boolean isTrustAllCertificatesEnabled() {
		return trustAllCertificatesEnabled;
	}

	@Override
	public final String getProductBaseURL() {
		return productBaseURL;
	}

	@Override
	public final String getTraceID() {
		return traceID;
	}

	@Override
	public String getTargetType() {
		if (targetType==null) {
			return "";
		}
		return targetType;
	}

	@Override
	public final Set<URI> getTargetURIs() {
		return targetURIs;
	}

	@Override
	public URI getTargetURI() {
		if (targetURIs==null || targetURIs.isEmpty()) {
			return null;
		}
		return targetURIs.iterator().next();
	}

	@Override
	public String getTargetAsString() {
		URI uri = getTargetURI();
		if (uri==null) {
			return null;
		}
		return uri.toString();
	}

	@Override
	public final Set<InetAddress> getTargetIPs() {
		return targetIPs;
	}

	@Override
	public Set<URI> getRootTargetURIs() {
		return rootTargetUris;
	}

	@Override
	public URI getRootTargetURI() {
		if (rootTargetUris==null || rootTargetUris.isEmpty()) {
			return null;
		}
		return rootTargetUris.iterator().next();
	}

	@Override
	public String getRootTargetURIasString() {
		URI uri = getRootTargetURI();
		if (uri==null) {
			return null;
		}
		return uri.toString();
	}

	@Override
	public final String getBase64Token() {
		return base64Token;
	}

	@Override
	public final String getUser() {
		return user;
	}

	@Override
	public String getPolicyId() {
		return policyId;
	}

	@Override
	public final String getPassword() {
		return CryptoAccess.CRYPTO_STRING.unseal(password);
	}

	@Override
	public final String getProxyHostname() {
		return proxyHostname;
	}

	@Override
	public final int getProxyPort() {
		return proxyPort;
	}

	@Override
	public final boolean isProxyDefined() {
		return proxyHostname != null && !proxyHostname.isEmpty();
	}

	@Override
	public Map<String, Object> getOptions() {
		return options;
	}

	@Override
	public String getProjectId() {
		return projectId;
	}

}
