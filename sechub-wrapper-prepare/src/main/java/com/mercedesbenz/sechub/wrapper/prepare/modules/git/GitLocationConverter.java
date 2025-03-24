// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import static com.mercedesbenz.sechub.wrapper.prepare.modules.UsageExceptionExitCode.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.pds.commons.core.PDSLogSanitizer;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperUsageException;

@Component
public class GitLocationConverter {

    private static final String PREFIX_HTTP = "http://";
    private static final String PREFIX_SSH = "ssh://";
    private static final String PREFIX_GIT = "git://";
    private static final String PREFIX_GIT_AT = "git@";
    private static final String HTTPS_PROTOCOL = "https";
    private static final String PREFIX_HTTPS_PROTOCOL = HTTPS_PROTOCOL + "://";

    private static final Logger LOG = LoggerFactory.getLogger(GitLocationConverter.class);

    private final PDSLogSanitizer pdsLogSanitizer;

    public GitLocationConverter(PDSLogSanitizer pdsLogSanitizer) {
        this.pdsLogSanitizer = pdsLogSanitizer;
    }

    public String convertLocationToRepositoryName(String location) {
        String[] parts = location.split("/");
        String repository = parts[parts.length - 1];
        repository = repository.replace(".git", "");
        return repository;
    }

    public URL convertLocationToHttpsBasedURL(String originLocation) {
        if (originLocation == null) {
            throw new IllegalArgumentException("Location may not be null!");
        }
        if (originLocation.isBlank()) {
            throw new IllegalArgumentException("Location may not be blank!");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Convert location: {} to HTTPS based URL", pdsLogSanitizer.sanitize(originLocation, 1024));
        }

        String urlLocation = originLocation;
        String formerPrefix = null;

        if (!urlLocation.startsWith(PREFIX_HTTPS_PROTOCOL)) {

            /* clone with password and user name does only work in JGit with URL */
            List<String> prefixes = List.of(PREFIX_GIT_AT, PREFIX_GIT, PREFIX_HTTP, PREFIX_SSH);
            for (String prefix : prefixes) {
                if (urlLocation.startsWith(prefix)) {
                    if (prefix.equals(PREFIX_GIT_AT)) {
                        urlLocation = urlLocation.replace(":", "/");
                    } else if (prefix.equals(PREFIX_GIT)) {
                        urlLocation = urlLocation.replace(":", "/");
                        urlLocation = urlLocation.replace("git///", "git://"); // revert unwanted prefix convert...
                    }
                    urlLocation = urlLocation.replace(prefix, PREFIX_HTTPS_PROTOCOL);
                    formerPrefix = prefix;
                    break;
                }
            }
        }

        return createURL(urlLocation, formerPrefix);
    }

    private URL createURL(String urlLocation, String formerPrefix) {
        try {
            URL url = new URL(urlLocation);
            if (PREFIX_SSH.equals(formerPrefix)) {
                // in this case we remove the port information because other protocol (we use
                // 443 for https)!
                url = new URL(HTTPS_PROTOCOL, url.getHost(), 443, url.getFile());
            }

            if (!url.getProtocol().equals(HTTPS_PROTOCOL)) {
                throw new PrepareWrapperUsageException(
                        "Location could not be transferred into a valid HTTPS URL: " + pdsLogSanitizer.sanitize(urlLocation, 1024), LOCATION_URL_NOT_VALID_URL);
            }
            return url;

        } catch (MalformedURLException e) {
            throw new PrepareWrapperUsageException("Location could not be transferred into a valid URL: " + pdsLogSanitizer.sanitize(urlLocation, 1024),
                    LOCATION_URL_NOT_VALID_URL);
        }
    }
}
