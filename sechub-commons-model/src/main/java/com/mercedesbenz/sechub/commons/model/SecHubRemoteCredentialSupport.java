package com.mercedesbenz.sechub.commons.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecHubRemoteCredentialSupport {
    private final String PDS_PREPARE_REMOTE_CREDENTIAL_LIST = "PDS_PREPARE_REMOTE_CREDENTIAL_LIST";
    private static final Logger LOG = LoggerFactory.getLogger(SecHubRemoteCredentialSupport.class);

    public List<SecHubRemoteCredentialContainer> resolveCredentialsForLocation(SecHubRemoteCredentialConfiguration configuration, String location) {
        return matchRemoteCredentialsFromLocationConfiguration(configuration, location, null);
    }

    public List<SecHubRemoteCredentialContainer> resolveCredentialsForLocation(SecHubRemoteCredentialConfiguration configuration, String location,
            String type) {
        TypeFilter.AcceptTypeFilter acceptTypeFilter = new TypeFilter.AcceptTypeFilter(type);
        return matchRemoteCredentialsFromLocationConfiguration(configuration, location, acceptTypeFilter);
    }

    public List<SecHubRemoteCredentialContainer> matchRemoteCredentialsFromLocationConfiguration(SecHubRemoteCredentialConfiguration configuration,
            String location, TypeFilter filter) {
        if (filter == null) {
            filter = TypeFilter.ACCEPT_ALL;
        }
        List<SecHubRemoteCredentialContainer> matchedCredentials = new ArrayList<>();

        if (location == null) {
            LOG.debug("Could not match credentials as configured location was null.");
            return matchedCredentials;
        }

        for (SecHubRemoteCredentialContainer credentialContainer : configuration.getCredentials()) {
            String stringPattern = credentialContainer.getRemotePattern();
            String type = credentialContainer.getType();

            if (stringPattern == null) {
                LOG.debug("Could not match credentials as configured remote pattern was null.");
                continue;
            }

            Pattern pattern = Pattern.compile(stringPattern);
            Matcher matcher = pattern.matcher(location);
            if (matcher.find() && filter.isTypeAccepted(type)) {
                matchedCredentials.add(credentialContainer);
            }
        }
        if (matchedCredentials.isEmpty()) {
            LOG.debug("Could not match any credentials for configured location {}", location);
        }
        return matchedCredentials;
    }

    public SecHubRemoteCredentialConfiguration getRemoteCredentialConfigurationFromJSONString(String json) {
        return SecHubRemoteCredentialConfiguration.fromJSONString(json);
    }

    public String readRemoteCredentialsFromEnv() {
        return System.getenv(PDS_PREPARE_REMOTE_CREDENTIAL_LIST);
    }
}
