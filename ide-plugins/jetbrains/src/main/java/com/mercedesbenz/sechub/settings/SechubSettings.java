// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.settings;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

@State(name = "com.mercedesbenz.sechub.settings.SechubSettings", storages = @Storage("SdkSettingsPlugin.xml"))
public final class SechubSettings implements PersistentStateComponent<SechubSettings.State> {

    private State state = new State();

    public static SechubSettings getInstance() {
        return ApplicationManager.getApplication().getService(SechubSettings.class);
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    public static class State {

        @NonNls
        public String serverURL = "";

        public boolean useCustomWebUiUrl = false;

        @NonNls
        public String webUiURL = "";

        public boolean sslTrustAll = false;
    }

}