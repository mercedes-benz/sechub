// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.settings;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import javax.swing.*;

import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.options.Configurable;
import com.mercedesbenz.sechub.plugin.idea.window.SecHubServerPanel;

/*
 * Provides controller functionality for application settings.
 */
final class SechubSettingsConfigurable implements Configurable {

    private SecHubSettingsComponent sechubSettingsComponent;
    private SechubSettingsCredentialsSupport sechubSettingsCredentialsSupport;

    // A default constructor with no arguments is required because
    // this implementation is registered as an applicationConfigurable

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "SecHub";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return sechubSettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        sechubSettingsComponent = new SecHubSettingsComponent();
        sechubSettingsCredentialsSupport = new SechubSettingsCredentialsSupport();
        return sechubSettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        SechubSettings.State state = Objects.requireNonNull(SechubSettings.getInstance().getState());
        String currentPassword = "";
        String currentUserName = "";

        Credentials credentials = sechubSettingsCredentialsSupport.retrieveCredentials();
        if (credentials != null) {
            currentPassword = credentials.getPasswordAsString();
            currentUserName = credentials.getUserName();
        }

        /* @formatter:off */
        return !sechubSettingsComponent.getUserNameText().equals(currentUserName) ||
                !sechubSettingsComponent.getApiTokenPassword().equals(currentPassword) ||
                !sechubSettingsComponent.getSecHubServerUrlText().equals(state.serverURL) ||
                !sechubSettingsComponent.useCustomWebUiUrl() == state.useCustomWebUiUrl ||
                !sechubSettingsComponent.getWebUiUrlText().equals(state.webUiURL) ||
                !sechubSettingsComponent.isSslTrustAll() == state.sslTrustAll;
        /* @formatter:on */
    }

    @Override
    public void apply() throws ConfigurationException {
        SechubSettings.State state = Objects.requireNonNull(SechubSettings.getInstance().getState());

        String serverUrl = sechubSettingsComponent.getSecHubServerUrlText();
        if (!serverUrl.isBlank() && !serverUrl.startsWith("http")) {
            /*
             * It is necessary to apply http protocol since the sechubClient needs it even
             * when the URI is valid
             */
            /* using URL instead of URI won't solve the problem */
            serverUrl = "https://" + serverUrl;
        }
        state.serverURL = serverUrl;

        if (!isUriValid(state.serverURL)) {
            throw new ConfigurationException("SecHub server URL must be a valid URI");
        }

        String username = sechubSettingsComponent.getUserNameText();
        String apiTokenPassword = sechubSettingsComponent.getApiTokenPassword();

        if (username.isBlank()) {
            throw new ConfigurationException("Username must not be empty");
        }

        if (apiTokenPassword.isBlank()) {
            throw new ConfigurationException("API token must not be empty");
        }

        Credentials credentials = new Credentials(username, apiTokenPassword);
        sechubSettingsCredentialsSupport.storeCredentials(credentials);

        state.useCustomWebUiUrl = sechubSettingsComponent.useCustomWebUiUrl();

        if (state.useCustomWebUiUrl && !isUriValid(sechubSettingsComponent.getWebUiUrlText())) {
            throw new ConfigurationException("Web UI URL must be a valid URI");
        }

        state.webUiURL = sechubSettingsComponent.getWebUiUrlText();

        state.sslTrustAll = sechubSettingsComponent.isSslTrustAll();

        updateComponents(state);
    }

    @Override
    public void reset() {
        SechubSettings.State state = Objects.requireNonNull(SechubSettings.getInstance().getState());
        sechubSettingsComponent.setSecHubServerUrlText(state.serverURL);

        Credentials credentials = sechubSettingsCredentialsSupport.retrieveCredentials();

        sechubSettingsComponent.setUseCustomWebUiUrl(state.useCustomWebUiUrl);

        sechubSettingsComponent.setWebUiUrlText(state.webUiURL);

        sechubSettingsComponent.setSslTrustAll(state.sslTrustAll);

        displayCredentialsInSettings(credentials);
    }

    private void displayCredentialsInSettings(Credentials credentials) {
        if (credentials != null) {
            String password = credentials.getPasswordAsString();
            String userName = credentials.getUserName();

            if (userName == null) {
                userName = "";
            }
            if (password == null) {
                password = "";
            }
            sechubSettingsComponent.setUserNameText(userName);
            sechubSettingsComponent.setApiTokenPassword(password);
        }
    }

    @Override
    public void disposeUIResources() {
        sechubSettingsComponent = null;
    }

    private static void updateComponents(SechubSettings.State state) {
        SecHubServerPanel secHubServerPanel = SecHubServerPanel.getInstance();
        secHubServerPanel.updateSettingsState(state);
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