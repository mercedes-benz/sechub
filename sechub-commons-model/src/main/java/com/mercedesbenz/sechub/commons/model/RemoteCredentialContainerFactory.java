package com.mercedesbenz.sechub.commons.model;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteCredentialContainerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(RemoteCredentialContainerFactory.class);

    public RemoteCredentialContainer create(RemoteCredentialConfiguration configuration) {

        Map<String, Pattern> patternMap = new TreeMap<>();
        for (RemoteCredentialData credentialContainer : configuration.getCredentials()) {
            String stringPattern = credentialContainer.getRemotePattern();

            if (stringPattern == null) {
                LOG.error("Could not match credentials as configured remote pattern was null.");
                continue;
            }

            try {
                Pattern pattern = Pattern.compile(stringPattern);
                patternMap.put(stringPattern, pattern);

            } catch (PatternSyntaxException e) {
                throw new IllegalStateException("Was not able to parse remote credential configuration.", e);
            }
        }
        return new RemoteCredentialContainer(configuration, patternMap);
    }
}
