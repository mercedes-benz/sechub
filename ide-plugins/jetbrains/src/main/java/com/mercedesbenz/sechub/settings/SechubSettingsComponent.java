// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.settings;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.net.URI;
import java.net.URISyntaxException;

public class SechubSettingsComponent {

    private final JPanel mainPanel;
    private final JBTextField userNameText = new JBTextField();
    private final JBTextField serverUrlText = new JBTextField();
    private final JBPasswordField apiTokenPassword = new JBPasswordField();

    public SechubSettingsComponent() {
        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Server URL:"), serverUrlText, 1, false)
                .addLabeledComponent(new JBLabel("User name:"), userNameText, 1, false)
                .addLabeledComponent(new JBLabel("API token:"), apiTokenPassword, 1, false)
                .addComponentFillVertically(new JPanel(), 0).getPanel();
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return userNameText;
    }

    @NotNull
    public String getUserNameText() {
        return userNameText.getText();
    }

    public void setUserNameText(@NotNull String newText) {
        userNameText.setText(newText);
    }

    @NotNull
    public String getServerUrlText() {
        assertServerURIValid();
        return serverUrlText.getText();
    }

    public void setServerUrlText(@NotNull String newText) {
        serverUrlText.setText(newText);
    }

    @NotNull
    public String getApiTokenPassword() {
        return String.valueOf(apiTokenPassword.getPassword());
    }

    public void setApiTokenPassword(@NotNull String newText) {
        apiTokenPassword.setText(newText);
    }

    private void assertServerURIValid() {
        String serverUrl = serverUrlText.getText();
        if (serverUrl.isBlank()) {
            serverUrlText.setBackground(JBColor.background());
            return;
        }
        try {
            new URI(serverUrl);
            serverUrlText.setBackground(JBColor.background());
        } catch (URISyntaxException e) {
            serverUrlText.setBackground(JBColor.RED);
        }
    }
}
