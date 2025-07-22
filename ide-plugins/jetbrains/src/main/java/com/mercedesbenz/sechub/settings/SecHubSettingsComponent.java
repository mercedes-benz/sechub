// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.settings;

import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.*;

import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.NotNull;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;

public class SecHubSettingsComponent {

    private final JPanel mainPanel;
    private final JBTextField userNameText = new JBTextField();
    private final JBTextField secHubServerUrlText = new JBTextField();
    private final JBPasswordField apiTokenPassword = new JBPasswordField();
    private final JBCheckBox useCustomWebUiUrl = new JBCheckBox();
    private final JBTextField webUiUrlText = new JBTextField();
    private final JBCheckBox sslTrustAll = new JBCheckBox();

    public SecHubSettingsComponent() {
        JBLabel webUiUrlLabel = new JBLabel("SecHub Web UI URL:");

        /* @formatter:off */
        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("SecHub Server URL:"), secHubServerUrlText, 1, false)
                .addLabeledComponent(new JBLabel("User name:"), userNameText, 1, false)
                .addLabeledComponent(new JBLabel("API token:"), apiTokenPassword, 1, false)
                .addLabeledComponent(new JBLabel("Use custom SecHub Web UI URL:"), useCustomWebUiUrl, 1, false)
                .addLabeledComponent(webUiUrlLabel, webUiUrlText, 1, false)
                .addLabeledComponent(new JBLabel("SSL trust all:"), sslTrustAll, 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        /* @formatter:on */

        useCustomWebUiUrl.addChangeListener(event -> {
            handleWebUiUrlVisibility(webUiUrlLabel);
        });

        handleWebUiUrlVisibility(webUiUrlLabel);
    }

    private void handleWebUiUrlVisibility(JBLabel webUiUrlLabel) {
        boolean isVisible = useCustomWebUiUrl.isSelected();
        webUiUrlLabel.setVisible(isVisible);
        webUiUrlText.setVisible(isVisible);
        webUiUrlText.setEnabled(isVisible);
        webUiUrlText.setEditable(isVisible);
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
    public String getSecHubServerUrlText() {
        return secHubServerUrlText.getText();
    }

    public void setSecHubServerUrlText(@NotNull String newText) {
        secHubServerUrlText.setText(newText);
    }

    @NotNull
    public String getApiTokenPassword() {
        return String.valueOf(apiTokenPassword.getPassword());
    }

    public void setApiTokenPassword(@NotNull String newText) {
        apiTokenPassword.setText(newText);
    }

    public boolean useCustomWebUiUrl() {
        return useCustomWebUiUrl.isSelected();
    }

    public void setUseCustomWebUiUrl(boolean useCustomWebUiUrl) {
        this.useCustomWebUiUrl.setSelected(useCustomWebUiUrl);
        webUiUrlText.setEnabled(useCustomWebUiUrl);
    }

    @NotNull
    public String getWebUiUrlText() {
        if (!useCustomWebUiUrl()) {
            /* This will route the user to the WebUI through the login page of the SecHub server */
            return secHubServerUrlText.getText() + "/login";
        }
        return webUiUrlText.getText();
    }

    public void setWebUiUrlText(@NotNull String newText) {
        webUiUrlText.setText(newText);
    }

    public boolean isSslTrustAll() {
        return sslTrustAll.isSelected();
    }

    public void setSslTrustAll(boolean sslTrustAll) {
        this.sslTrustAll.setSelected(sslTrustAll);
    }
}
