// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.sechubaccess;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import com.intellij.credentialStore.Credentials;
import com.mercedesbenz.sechub.plugin.idea.SecHubSettingsDialogListener;
import com.mercedesbenz.sechub.settings.SechubSettings;
import com.mercedesbenz.sechub.settings.SechubSettingsCredentialsSupport;

import static org.apache.commons.lang3.ObjectUtils.anyNull;

public class SecHubAccessFactory {
    public static SecHubAccess create() {
        SechubSettingsCredentialsSupport sechubSettingsCredentialsSupport = new SechubSettingsCredentialsSupport();
        Credentials credentials = sechubSettingsCredentialsSupport.retrieveCredentials();
        SecHubSettingsDialogListener settingsDialogListener = SecHubSettingsDialogListener.getInstance();
        SechubSettings.State state = Objects.requireNonNull(SechubSettings.getInstance().getState());

        if (anyNull(state, credentials)) {
            settingsDialogListener.onShowSettingsDialog();
        }

        if (anyNull(state.serverURL, credentials.getUserName(), credentials.getPasswordAsString())) {
            settingsDialogListener.onShowSettingsDialog();
        }

        if (!isUriValid(state.serverURL)) {
            settingsDialogListener.onShowSettingsDialog();
        }

        return new SecHubAccess(state.serverURL, credentials.getUserName(), credentials.getPasswordAsString(), state.sslTrustAll);
    }

    private static boolean isUriValid(String uri) {
        if (uri == null || uri.isBlank()) {
            return false;
        }
        try {
            URI parsed = new URI(uri);
            if (parsed.getScheme() == null || parsed.getScheme().isBlank()) {
                return false;
            }
            if (parsed.getHost() == null || parsed.getHost().isBlank()) {
                return false;
            }
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
