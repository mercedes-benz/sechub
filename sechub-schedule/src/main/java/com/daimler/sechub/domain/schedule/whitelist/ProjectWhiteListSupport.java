// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.whitelist;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import org.springframework.stereotype.Component;

import com.daimler.sechub.commons.core.util.SimpleStringUtils;
import com.daimler.sechub.sharedkernel.error.NotAcceptableException;

@Component
public class ProjectWhiteListSupport {

	public boolean isWhitelisted(String uriAsString, Collection<URI> whitelist) {
		URI uri;
		try {
			uri = new URI(uriAsString);
		} catch (URISyntaxException e) {
			throw new NotAcceptableException("Not a valid uri:"+uriAsString);
		}
		for (URI accepted: whitelist) {
			if (isContained(uri, accepted)) {
				return true;
			}
		}
		
		return false;
	}

	private boolean isContained(URI toTest, URI accepted) {
		boolean isContained =  toTest!=null;
		isContained =  isContained && accepted!=null;
		isContained =  isContained && sameHost(toTest,accepted);
		isContained =  isContained && samePort(toTest, accepted);
		isContained =  isContained && sameProtokoll(toTest, accepted);
		isContained =  isContained && sameBasePath(toTest, accepted);
		return isContained;
	}

	private boolean sameBasePath(URI toTest, URI accepted) {
		return SimpleStringUtils.startsWith(accepted.getPath(), toTest.getPath());
	}

	private boolean samePort(URI toTest, URI accepted) {
		return toTest.getPort()== accepted.getPort();
	}
	
	private boolean sameProtokoll(URI toTest, URI accepted) {
		return SimpleStringUtils.equals(toTest.getScheme(), accepted.getScheme());
	}

	private boolean sameHost(URI toTest, URI accepted) {
		return SimpleStringUtils.equals(toTest.getHost(), accepted.getHost());
	}
	
	

	
}
