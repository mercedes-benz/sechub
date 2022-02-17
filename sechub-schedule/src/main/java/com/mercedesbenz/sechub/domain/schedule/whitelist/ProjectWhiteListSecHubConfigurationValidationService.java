// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.whitelist;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubInfrastructureScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.domain.schedule.whitelist.ProjectWhitelistEntry.ProjectWhiteListEntryCompositeKey;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;

@Service
public class ProjectWhiteListSecHubConfigurationValidationService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectWhiteListSecHubConfigurationValidationService.class);

    @Autowired
    ProjectWhitelistEntryRepository projectWhiteListEntryRepository;

    @Autowired
    ProjectWhiteListSupport support;

    @Autowired
    LogSanitizer logSanitizer;

    public void assertAllowedForProject(SecHubConfigurationModel configuration) {
        List<URI> allowed = fetchAllowedUris(configuration);

        Optional<SecHubInfrastructureScanConfiguration> infrascanOpt = configuration.getInfraScan();
        if (infrascanOpt.isPresent()) {
            SecHubInfrastructureScanConfiguration infraconf = infrascanOpt.get();
            assertWhitelisted(allowed, infraconf.getUris());
            assertWhitelisted(allowed, asUris(infraconf.getIps()));
        }

        Optional<SecHubWebScanConfiguration> webscanopt = configuration.getWebScan();
        if (webscanopt.isPresent()) {
            SecHubWebScanConfiguration webconf = webscanopt.get();
            assertWhitelisted(allowed, webconf.getUri());
        }

    }

    private List<URI> asUris(List<InetAddress> ips) {
        List<URI> list = new ArrayList<>();
        for (InetAddress ip : ips) {
            try {
                list.add(new URI(ip.getHostAddress()));
            } catch (URISyntaxException e) {
                throw new IllegalStateException("An URI must be creatable by an IP-Adress?!?! IP was:" + ip, e);
            }
        }
        return list;
    }

    private void assertWhitelisted(List<URI> allowed, List<URI> wanted) {
        for (URI uri : wanted) {
            if (!support.isWhitelisted(uri.toString(), allowed)) {
                throw new NotAcceptableException("URI not whitelisted in project:" + uri);
            }
        }
    }

    private void assertWhitelisted(List<URI> allowed, URI targetUri) {
        if (!support.isWhitelisted(targetUri.toString(), allowed)) {
            throw new NotAcceptableException("URI not whitelisted in project:" + targetUri);
        }
    }

    private List<URI> fetchAllowedUris(SecHubConfigurationModel configuration) {
        List<ProjectWhitelistEntry> whiteListEntries = projectWhiteListEntryRepository.fetchWhiteListEntriesForProject(configuration.getProjectId());
        List<URI> list = new ArrayList<>();
        for (ProjectWhitelistEntry entry : whiteListEntries) {
            if (entry == null) {
                LOG.warn("Found null entry inside whitelist for project:{}. In this case, please update whitelist entries and remove empty ones!",
                        logSanitizer.sanitize(configuration.getProjectId(), -1));
                continue;
            }
            ProjectWhiteListEntryCompositeKey key = entry.getKey();
            list.add(key.getUri());
        }
        return list;
    }

}
