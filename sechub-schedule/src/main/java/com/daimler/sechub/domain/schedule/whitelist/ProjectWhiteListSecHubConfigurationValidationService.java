// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.whitelist;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.configuration.SecHubInfrastructureScanConfiguration;
import com.daimler.sechub.sharedkernel.configuration.SecHubWebScanConfiguration;
import com.daimler.sechub.sharedkernel.error.NotAcceptableException;

@Service
public class ProjectWhiteListSecHubConfigurationValidationService {

	@Autowired
	ProjectWhitelistEntryRepository projectWhiteListEntryRepository;
	
	@Autowired
	ProjectWhiteListSupport support;
	
	public void assertAllowedForProject(SecHubConfiguration configuration) {
		List<ProjectWhitelistEntry> whiteListEntries = projectWhiteListEntryRepository.fetchWhiteListEntriesForProject(configuration.getProjectId());
		List<URI> allowed = fetchAllowedUris(whiteListEntries);
		
		Optional<SecHubInfrastructureScanConfiguration> infrascanOpt = configuration.getInfraScan();
		if (infrascanOpt.isPresent()) {
			SecHubInfrastructureScanConfiguration infraconf = infrascanOpt.get();
			assertWhitelisted(allowed, infraconf.getUris());
			assertWhitelisted(allowed, asUris(infraconf.getIps()));
		}
		Optional<SecHubWebScanConfiguration> webscanopt = configuration.getWebScan();
		if (webscanopt.isPresent()) {
			SecHubWebScanConfiguration webconf = webscanopt.get();
			assertWhitelisted(allowed, webconf.getUris());
		}
		
	}

	private List<URI> asUris(List<InetAddress> ips) {
		List<URI> list = new ArrayList<>();
		for (InetAddress ip: ips) {
			try {
				list.add(new URI(ip.getHostAddress()));
			} catch (URISyntaxException e) {
				throw new IllegalStateException("An URI must be creatable by an IP-Adress?!?! IP was:"+ip, e);
			}
		}
		return list;
	}

	private void assertWhitelisted(List<URI> allowed, List<URI> wanted) {
		for (URI uri: wanted) {
			if (! support.isWhitelisted(uri.toString(), allowed)) {
				throw new NotAcceptableException("URI not whitelisted in project:"+uri);
			}
		}
	}

	private List<URI> fetchAllowedUris(List<ProjectWhitelistEntry> whiteListEntries) {
		List<URI> list = new ArrayList<>();
		for (ProjectWhitelistEntry entry: whiteListEntries) {
			list.add(entry.getKey().getUri());
		}
		return list;
	}
	
}
