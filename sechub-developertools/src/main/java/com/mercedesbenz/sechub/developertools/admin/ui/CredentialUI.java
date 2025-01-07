// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.mercedesbenz.sechub.developertools.admin.DeveloperAdministration;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;

public class CredentialUI {
    JPasswordField passwordField;
    JTextField useridField;
    JTextField serverField;
    JTextField protocol;
    JSpinner serverPortSpinner;
    JTextField protocolField;
    private JPanel panel;

    public JPanel getPanel() {
        return panel;
    }

    public CredentialUI(UIContext context) {
        String port = ConfigurationSetup.SECHUB_ADMIN_SERVER_PORT.getStringValue("443");
        String protocol = ConfigurationSetup.SECHUB_ADMIN_SERVER_PROTOCOL.getStringValue("https");
        String server = ConfigurationSetup.SECHUB_ADMIN_SERVER.getStringValueOrFail();
        String userId = ConfigurationSetup.SECHUB_ADMIN_USERID.getStringValueOrFail();
        String apiToken = ConfigurationSetup.SECHUB_ADMIN_APITOKEN.getStringValueOrFail();

        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        useridField = new JTextField(userId);
        passwordField = new JPasswordField(apiToken);
        serverField = new JTextField(server);

        protocolField = new JTextField(protocol);

        serverPortSpinner = new JSpinner(new SpinnerNumberModel());
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(serverPortSpinner);
        editor.getFormat().setGroupingUsed(false);
        serverPortSpinner.setEditor(editor);
        int portNumber = Integer.valueOf(port).intValue();
        serverPortSpinner.setValue(portNumber);

        /*
         * when we run integration test server mode, we use the passwords from
         * integration test super admin
         */
        if (ConfigurationSetup.isIntegrationTestServerMenuEnabled()) {
            useridField.setText(TestAPI.SUPER_ADMIN.getUserId());
            passwordField.setText(TestAPI.SUPER_ADMIN.getApiToken());
        }

        serverField.setPreferredSize(new Dimension(300, 30));
        useridField.setPreferredSize(new Dimension(200, 30));
        passwordField.setPreferredSize(new Dimension(200, 30));
        serverPortSpinner.setPreferredSize(new Dimension(100, 30));

        panel.add(new JLabel("Server:"));
        panel.add(protocolField);
        panel.add(serverField);
        panel.add(new JLabel("Port:"));
        panel.add(serverPortSpinner);
        panel.add(new JLabel("User:"));
        panel.add(useridField);
        panel.add(new JLabel("API-Token:"));
        panel.add(passwordField);

        /* bridge to TEST API ... we need server and user data available by test api */
        DeveloperAdministration administration = context.getAdministration();
        administration.updateTestAPIServerConnection(server, portNumber);
        administration.updateTestAPISuperAdmin(userId, apiToken);

        /*
         * currently there is a bug - changes are not handled . So we disable edit
         * fields etc.
         */
        serverField.setEnabled(false);
        useridField.setEnabled(false);
        passwordField.setEnabled(false);
        serverPortSpinner.setEnabled(false);
        protocolField.setEnabled(false);

        serverField.setToolTipText(ConfigurationSetup.SECHUB_ADMIN_SERVER.getSystemPropertyId());
        useridField.setToolTipText(ConfigurationSetup.SECHUB_ADMIN_USERID.getSystemPropertyId());
        passwordField.setToolTipText(ConfigurationSetup.SECHUB_ADMIN_APITOKEN.getSystemPropertyId());
        serverPortSpinner.setToolTipText(ConfigurationSetup.SECHUB_ADMIN_SERVER_PORT.getSystemPropertyId());
        protocolField.setToolTipText(ConfigurationSetup.SECHUB_ADMIN_SERVER_PROTOCOL.getSystemPropertyId());

        useDifferentColorsForWellknownEnvironments();

    }

    private void useDifferentColorsForWellknownEnvironments() {
        /* colourize for special environments - if set */
        String env = ConfigurationSetup.SECHUB_ADMIN_ENVIRONMENT.getStringValueOrFail();
        if ("PROD".equalsIgnoreCase(env) || "PRODUCTION".equalsIgnoreCase(env)) {
            panel.setBackground(new Color(200, 110, 110));
            panel.setForeground(Color.WHITE);
        } else if ("INT".equalsIgnoreCase(env) || "INTEGRATION".equalsIgnoreCase(env)) {
            panel.setBackground(new Color(200, 200, 110));
        } else if (env.toLowerCase().indexOf("test") != -1) {
            panel.setBackground(new Color(110, 200, 200));
        }
    }

    public int getPortNumber() {
        return ((Integer) serverPortSpinner.getValue()).intValue();
    }
}
