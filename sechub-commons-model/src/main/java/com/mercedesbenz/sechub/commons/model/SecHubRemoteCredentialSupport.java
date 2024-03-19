package com.mercedesbenz.sechub.commons.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecHubRemoteCredentialSupport {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubRemoteCredentialSupport.class);

    public List<SecHubRemoteCredentialContainer> matchRemoteCredentialsFromLocation(SecHubRemoteCredentialConfiguration configuration, String location) {
        List<SecHubRemoteCredentialContainer> matchedCredentials = new ArrayList<>();

        if (location == null) {
            logEmptyOrNUllValue("location", "null");
            return matchedCredentials;
        }

        for (SecHubRemoteCredentialContainer credentialContainer : configuration.getCredentials()) {
            String stringPattern = credentialContainer.getRemotePattern();

            if (stringPattern == null) {
                logEmptyOrNUllValue("remotePattern", "null");
                continue;
            }

            Pattern pattern = Pattern.compile(stringPattern);
            Matcher matcher = pattern.matcher(location);
            if (matcher.find()) {
                matchedCredentials.add(credentialContainer);
            }
        }
        if (matchedCredentials.isEmpty()) {
            logNoCredentialsFound("location", location);
        }
        return matchedCredentials;
    }

    public List<SecHubRemoteCredentialContainer> matchRemoteCredentialsFromType(SecHubRemoteCredentialConfiguration configuration, String type) {
        List<SecHubRemoteCredentialContainer> matchedCredentials = new ArrayList<>();
        // TODO: 19.03.24 laura what about ENUM for remote types (GIT, DOCKER etc. )
        for (SecHubRemoteCredentialContainer credentialContainer : configuration.getCredentials()) {
            String remoteType = credentialContainer.getType();
            if (remoteType == null || remoteType.isEmpty()) {
                logEmptyOrNUllValue("remote credential type", "null or empty");
                continue;
            }

            /* configuration can have more than one type defined: type1,type2 */
            final String[] splitTypes = remoteType.split(",");
            for (String splitType : splitTypes) {
                if (splitType.equals(type)) {
                    matchedCredentials.add(credentialContainer);
                }
            }
        }

        if (matchedCredentials.isEmpty()) {
            logNoCredentialsFound("type", type);
        }
        return matchedCredentials;
    }

    public SecHubRemoteCredentialConfiguration getRemoteCredentialConfigurationFromJSONString(String json) {
        return SecHubRemoteCredentialConfiguration.fromJSONString(json);
    }

    private void logNoCredentialsFound(String element, String value) {
        LOG.debug("Could not find credentials for \"{}\": \"{}\"", element, value);
    }

    private void logEmptyOrNUllValue(String element, String value) {
        LOG.debug("Could not match credentials for {}, as value was {}", element, value);
    }
}
