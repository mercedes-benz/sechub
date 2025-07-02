// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.sechubaccess;

import java.util.Objects;

import com.intellij.credentialStore.Credentials;
import com.mercedesbenz.sechub.settings.SechubSettings;
import com.mercedesbenz.sechub.settings.SechubSettingsCredentialsSupport;

public class SecHubAccessFactory {
    public static SecHubAccess create() {
        SechubSettingsCredentialsSupport sechubSettingsCredentialsSupport = new SechubSettingsCredentialsSupport();
        Credentials credentials = sechubSettingsCredentialsSupport.retrieveCredentials();
        SechubSettings.State state = Objects.requireNonNull(SechubSettings.getInstance().getState());

        return new SecHubAccess(state.serverURL, credentials.getUserName(), credentials.getPasswordAsString(), true);
    }
}
