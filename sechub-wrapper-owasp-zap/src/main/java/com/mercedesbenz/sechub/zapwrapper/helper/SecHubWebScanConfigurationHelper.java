// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.helper;

import com.mercedesbenz.sechub.commons.model.SecHubTimeUnit;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;
import com.mercedesbenz.sechub.zapwrapper.config.auth.AuthenticationType;

public class SecHubWebScanConfigurationHelper {
    private static final int SECONDS_IN_MS = 1000;
    private static final int MINUTES_IN_MS = 60 * SECONDS_IN_MS;
    private static final int HOURS_IN_MS = 60 * MINUTES_IN_MS;
    private static final int DAYS_IN_MS = 24 * HOURS_IN_MS;

    private static final int DEFAULT_MAX_SCAN_DURATION = 8 * HOURS_IN_MS;

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

    public long fetchMaxScanDurationInMillis(SecHubWebScanConfiguration sechubWebConfig) {
        if (!sechubWebConfig.getMaxScanDuration().isPresent()) {
            return DEFAULT_MAX_SCAN_DURATION;
        }

        SecHubTimeUnit sechubTimeUnit = sechubWebConfig.getMaxScanDuration().get().getUnit();
        int maxScanDuration = sechubWebConfig.getMaxScanDuration().get().getDuration();

        switch (sechubTimeUnit) {
        case DAY:
            return maxScanDuration * DAYS_IN_MS;
        case HOUR:
            return maxScanDuration * HOURS_IN_MS;
        case MINUTE:
            return maxScanDuration * MINUTES_IN_MS;
        case SECOND:
            return maxScanDuration * SECONDS_IN_MS;
        case MILLISECOND:
            return maxScanDuration;
        default:
            return DEFAULT_MAX_SCAN_DURATION;
        }
    }

}
