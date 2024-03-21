package com.mercedesbenz.sechub.commons.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteCredentialContainer {
    private static final Logger LOG = LoggerFactory.getLogger(RemoteCredentialContainer.class);

    private final RemoteCredentialConfiguration configuration;
    private final Map<String, Pattern> patternMap;

    public RemoteCredentialContainer(RemoteCredentialConfiguration configuration, Map<String, Pattern> patternMap) {
        this.configuration = configuration;
        this.patternMap = patternMap;
    }

    public List<RemoteCredentialData> resolveCredentialsForLocation(String location) {
        return resolveCredentialsFromLocation(location, null);
    }

    public List<RemoteCredentialData> resolveCredentialsForLocation(String location, String type) {
        return resolveCredentialsFromLocation(location, type == null ? null : t -> t.equals(type));
    }

    private List<RemoteCredentialData> resolveCredentialsFromLocation(String location, TypeFilter filter) {
        if (filter == null) {
            filter = TypeFilter.ACCEPT_ALL;
        }
        List<RemoteCredentialData> result = new ArrayList<>();

        if (location == null) {
            LOG.debug("Could not match credentials as configured location was null.");
            return result;
        }

        for (RemoteCredentialData credentialContainer : configuration.getCredentials()) {
            resolveForCredentialContainer(location, filter, credentialContainer, result);
        }

        if (result.isEmpty()) {
            LOG.debug("Could not match any credentials for configured location {}", location);
        }
        return result;
    }

    private void resolveForCredentialContainer(String location, TypeFilter filter, RemoteCredentialData credentialContainer,
            List<RemoteCredentialData> result) {
        List<String> types = credentialContainer.getTypes();

        boolean typeAccepted = false;
        /*
         * if no specific types are configured ,types must still be matched if filter
         * was ALL_TYPES
         */
        if (TypeFilter.ACCEPT_ALL.equals(filter)) {
            typeAccepted = true;
        }
        for (String type : types) {
            typeAccepted = filter.isTypeAccepted(type);
            if (typeAccepted) {
                break;
            }
        }
        if (!typeAccepted) {
            return;
        }

        String stringPattern = credentialContainer.getRemotePattern();
        if (stringPattern == null) {
            LOG.debug("Could not match credentials as configured remote pattern was null.");
            return;
        }

        Pattern pattern = patternMap.get(stringPattern);
        if (pattern == null) {
            throw new IllegalStateException("No Pattern found for: " + stringPattern);
        }

        Matcher matcher = pattern.matcher(location);
        if (matcher.find()) {
            result.add(credentialContainer);
        }
    }

}
