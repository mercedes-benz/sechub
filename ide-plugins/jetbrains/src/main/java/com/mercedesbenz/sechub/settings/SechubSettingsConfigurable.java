// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.settings;

import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.options.Configurable;
import com.mercedesbenz.sechub.plugin.idea.sechubaccess.SecHubAccess;
import com.mercedesbenz.sechub.plugin.idea.sechubaccess.SecHubAccessFactory;
import com.mercedesbenz.sechub.plugin.idea.window.SecHubServerPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

/*
 * Provides controller functionality for application settings.
 */
final class SechubSettingsConfigurable implements Configurable {

    private SechubSettingsComponent sechubSettingsComponent;
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
        sechubSettingsComponent = new SechubSettingsComponent();
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

        return !sechubSettingsComponent.getUserNameText().equals(currentUserName) ||
                !sechubSettingsComponent.getApiTokenPassword().equals(currentPassword) ||
                !sechubSettingsComponent.getServerUrlText().equals(state.serverURL);
    }

    @Override
    public void apply() {
        SechubSettings.State state = Objects.requireNonNull(SechubSettings.getInstance().getState());

        String serverUrl = sechubSettingsComponent.getServerUrlText();
        if (!serverUrl.isBlank() && !serverUrl.startsWith("http")) {
            /* It is necessary to apply http protocol since the sechubClient needs it even when the URI is valid */
            /* using URL instead of URI won't solve the problem */
            serverUrl = "https://" + serverUrl;
        }
        state.serverURL = serverUrl;

        Credentials credentials = new Credentials(sechubSettingsComponent.getUserNameText(), sechubSettingsComponent.getApiTokenPassword());
        sechubSettingsCredentialsSupport.storeCredentials(credentials);

        updateComponents(state);
    }



    @Override
    public void reset() {
        SechubSettings.State state = Objects.requireNonNull(SechubSettings.getInstance().getState());
        sechubSettingsComponent.setServerUrlText(state.serverURL);

        Credentials credentials = sechubSettingsCredentialsSupport.retrieveCredentials();

        displayCredentialsInSettings(credentials);
    }

    private void displayCredentialsInSettings(Credentials credentials) {
        if (credentials != null) {
            String password = credentials.getPasswordAsString();
            String userName = credentials.getUserName();

            if(userName == null){
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
        // Creating new sechubAccess with new client
        SecHubAccess secHubAccess = SecHubAccessFactory.create();

        // Updating the server URL in the SecHubServerPanel and check alive status
        SecHubServerPanel secHubServerPanel = SecHubServerPanel.getInstance();
        secHubServerPanel.update(state.serverURL, secHubAccess.isSecHubServerAlive());
    }
}