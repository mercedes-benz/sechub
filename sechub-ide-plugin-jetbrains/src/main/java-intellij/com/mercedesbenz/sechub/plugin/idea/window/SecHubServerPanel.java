// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.window;

import java.awt.*;
import java.util.Objects;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.mercedesbenz.sechub.plugin.idea.sechubaccess.SecHubAccess;
import com.mercedesbenz.sechub.plugin.idea.sechubaccess.SecHubAccessFactory;
import com.mercedesbenz.sechub.settings.SechubSettings;

public class SecHubServerPanel implements SecHubPanel {

    private static final Logger LOG = Logger.getInstance(SecHubServerPanel.class);
    public static final String SERVER_URL_NOT_CONFIGURED = "Server URL not configured";
    public static final String BUTTON_TEXT = "check connection";
    public static final String SERVER_URL_LABEL = "Server URL: ";
    public static final String SERVER_CONNECTION_LABEL = "Server connection: ";
    private static SecHubServerPanel INSTANCE;

    private JPanel contentPanel;
    private JBTextField serverUrlText;
    private final JLabel serverActiveLabel = new JBLabel();
    private final @NotNull Icon serverActiveUnchecked = AllIcons.Actions.Refresh;
    private final @NotNull Icon serverActiveTrue = AllIcons.General.InspectionsOK;
    private final @NotNull Icon serverActiveFalse = AllIcons.Ide.ErrorPoint;
    private final JButton serverActiveButton = new JButton(BUTTON_TEXT);

    public SecHubServerPanel() {
        createComponents();
    }

    public static void registerInstance(SecHubServerPanel secHubToolWindow) {
        LOG.info("Register tool windows instance:" + secHubToolWindow);
        INSTANCE = secHubToolWindow;
    }

    public static SecHubServerPanel getInstance() {
        return INSTANCE;
    }

    @Override
    public JPanel getContent() {
        return contentPanel;
    }

    public void update(String serverURL, boolean isActive) {
        if (serverURL.isBlank()) {
            serverURL = SERVER_URL_NOT_CONFIGURED;
        }
        serverUrlText.setText(serverURL);

        if (isActive) {
            serverActiveLabel.setIcon(serverActiveTrue);
        } else {
            serverActiveLabel.setIcon(serverActiveFalse);
        }
    }

    private void createComponents() {
        contentPanel = new JBPanel<>();
        contentPanel.setLayout(new BorderLayout());

        final JPanel content = createContentPanel();
        contentPanel.add(content, BorderLayout.NORTH);
    }

    @NotNull
    private JPanel createContentPanel() {
        JPanel content = new JPanel(new BorderLayout());
        serverUrlText = new JBTextField();

        serverUrlText.setEditable(false);
        String serverURL = Objects.requireNonNull(SechubSettings.getInstance().getState()).serverURL;
        if (serverURL.isBlank()) {
            serverURL = SERVER_URL_NOT_CONFIGURED;
        }
        serverUrlText.setText(serverURL);

        JPanel serverStatePanel = new JPanel();
        serverStatePanel.setLayout(new BorderLayout());

        addActionListenerToButton();

        serverActiveLabel.setIcon(serverActiveUnchecked);
        serverStatePanel.add(serverActiveLabel, BorderLayout.WEST);
        serverStatePanel.add(serverActiveButton, BorderLayout.EAST);

        JPanel labelPane = new JPanel();
        labelPane.setLayout(new GridLayout(0, 1));
        labelPane.add(new JBLabel(SERVER_URL_LABEL));
        labelPane.add(new JBLabel(SERVER_CONNECTION_LABEL));

        JPanel fieldPane = new JPanel();
        fieldPane.setLayout(new GridLayout(0, 1));
        fieldPane.add(serverUrlText);
        fieldPane.add(serverStatePanel);

        content.add(labelPane, BorderLayout.WEST);
        content.add(fieldPane, BorderLayout.CENTER);
        return content;
    }

    private void addActionListenerToButton() {
        serverActiveButton.addActionListener(e -> {
            SecHubAccess secHubAccess = SecHubAccessFactory.create();
            update(Objects.requireNonNull(SechubSettings.getInstance().getState()).serverURL, secHubAccess.isSecHubServerAlive());
        });
    }
}
