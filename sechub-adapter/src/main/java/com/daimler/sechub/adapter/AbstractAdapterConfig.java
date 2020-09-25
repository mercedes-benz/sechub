// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import java.net.InetAddress;
import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.crypto.SealedObject;

import com.daimler.sechub.commons.core.security.CryptoAccess;

public abstract class AbstractAdapterConfig implements AdapterConfig {

	String productBaseURL;
	
	private SealedObject passwordOrAPITokenBase64encoded;

	int timeToWaitForNextCheckOperationInMilliseconds;
	/* TODO Albert Tregnaghi, 2019-12-04: at the moment the time out settings seems to be not used. Also its corelated with check results. There is something odd should be checked*/
	int timeOutInMilliseconds;
	int proxyPort;
	String proxyHostname;

	String user;
	SealedObject passwordOrAPIToken;

	String policyId;

	String projectId;

	LinkedHashSet<URI> targetURIs=new LinkedHashSet<>();
	LinkedHashSet<URI> rootTargetUris = new LinkedHashSet<>();

	String traceID;
	boolean trustAllCertificatesEnabled;
	private Map<AdapterOptionKey, String> options = new HashMap<>();
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
	public final String getPasswordOrAPITokenBase64Encoded() {
		if (passwordOrAPIToken==null) {
			return null;
		}
		if (passwordOrAPITokenBase64encoded==null) {
			String tokenString = user + ":" + CryptoAccess.CRYPTO_STRING.unseal(passwordOrAPIToken);
			byte[] tokenBytes = tokenString.getBytes();
			passwordOrAPITokenBase64encoded =  CryptoAccess.CRYPTO_STRING.seal(Base64.getEncoder().encodeToString(tokenBytes));
		}
		return CryptoAccess.CRYPTO_STRING.unseal(passwordOrAPITokenBase64encoded);
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
	public final String getPasswordOrAPIToken() {
		return CryptoAccess.CRYPTO_STRING.unseal(passwordOrAPIToken);
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
	public Map<AdapterOptionKey, String> getOptions() {
		return options;
	}

	@Override
	public String getProjectId() {
		return projectId;
	}

}
