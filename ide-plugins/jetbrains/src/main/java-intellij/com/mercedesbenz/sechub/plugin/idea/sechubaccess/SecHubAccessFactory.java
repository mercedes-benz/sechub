// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.sechubaccess;

import com.intellij.credentialStore.Credentials;
import com.mercedesbenz.sechub.settings.SechubSettings;
import com.mercedesbenz.sechub.settings.SechubSettingsCredentialsSupport;

public class SecHubAccessFactory {
    public static SecHubAccess create() {
        SechubSettingsCredentialsSupport sechubSettingsCredentialsSupport = new SechubSettingsCredentialsSupport();
        Credentials credentials = sechubSettingsCredentialsSupport.retrieveCredentials();
        SechubSettings.State state = SechubSettings.getInstance().getState();
        if (isInvalidState(state, credentials)) {
            return new NoOpSecHubAccessClient();
        }
        return new SecHubAccessClient(state.serverURL, credentials.getUserName(), credentials.getPasswordAsString(), state.sslTrustAll);
    }

    private static boolean isInvalidState(SechubSettings.State state, Credentials credentials) {
        /* @formatter:off */
        return state == null ||
                state.serverURL == null ||
                state.serverURL.isBlank() ||
                credentials == null ||
                credentials.getUserName() == null ||
                credentials.getPassword() == null;
        /* @formatter:on */
    }


}
