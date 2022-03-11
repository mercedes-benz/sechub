// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.helper;

import com.mercedesbenz.sechub.commons.model.SecHubTimeUnit;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;
import com.mercedesbenz.sechub.owaspzapwrapper.config.auth.AuthenticationType;

public class SecHubWebScanConfigurationHelper {
    private static final int DEFAULT_MAX_SCAN_DURATION = 28800000; // 8 hours in milliseconds

    public AuthenticationType determineAuthenticationType(SecHubWebScanConfiguration secHubWebScanConfiguration) {
        if (secHubWebScanConfiguration == null) {
            return AuthenticationType.UNAUTHENTICATED;
        }
        if (!secHubWebScanConfiguration.getLogin().isPresent()) {
            return AuthenticationType.UNAUTHENTICATED;
        }

        WebLoginConfiguration webLoginConfiguration = secHubWebScanConfiguration.getLogin().get();
        if (webLoginConfiguration.getBasic().isPresent()) {
            return AuthenticationType.HTTP_BASIC_AUTHENTICATION;
        }
        return AuthenticationType.UNAUTHENTICATED;
    }

    public long retrieveMaxScanDurationInMillis(SecHubWebScanConfiguration sechubWebConfig) {
        if (!sechubWebConfig.getMaxScanDuration().isPresent()) {
            return DEFAULT_MAX_SCAN_DURATION;
        }

        SecHubTimeUnit sechubTimeUnit = sechubWebConfig.getMaxScanDuration().get().getUnit();
        int maxScanDuration = sechubWebConfig.getMaxScanDuration().get().getDuration();

        switch (sechubTimeUnit) {
        case DAY:
            return maxScanDuration * 1000 * 60 * 60 * 24;
        case HOUR:
            return maxScanDuration * 1000 * 60 * 60;
        case MINUTE:
            return maxScanDuration * 1000 * 60;
        case SECOND:
            return maxScanDuration * 1000;
        case MILLISECOND:
            return maxScanDuration;
        default:
            return DEFAULT_MAX_SCAN_DURATION;
        }
    }

}
