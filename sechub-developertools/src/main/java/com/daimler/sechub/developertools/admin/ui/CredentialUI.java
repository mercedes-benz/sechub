// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.daimler.sechub.integrationtest.api.TestAPI;

public class CredentialUI {
	JPasswordField passwordField;
	JTextField useridField;
	JTextField serverField;
	JSpinner serverPortSpinner;
	private JPanel panel;

	public JPanel getPanel() {
		return panel;
	}

	public CredentialUI() {
		String port = System.getProperty(ConfigurationSetup.ADMIN_SERVER_PORT.getId(),"443");
		String userId = System.getProperty(ConfigurationSetup.ADMIN_USERNAME.getId(), "");
		String apiToken = System.getProperty(ConfigurationSetup.ADMIN_APITOKEN.getId(), "");
		String server = System.getProperty(ConfigurationSetup.ADMIN_SERVER.getId(), "");

		assertNotEmpty(userId,"userid");
		assertNotEmpty(apiToken,"apitoken");
		assertNotEmpty(server,"server");

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//		panel.setBorder(BorderFactory.createLineBorder(Color.RED));
		useridField= new JTextField(userId);
		passwordField= new JPasswordField(apiToken);
		serverField = new JTextField(server);

		serverPortSpinner = new JSpinner(new SpinnerNumberModel());
		JSpinner.NumberEditor editor = new JSpinner.NumberEditor(serverPortSpinner);
		editor.getFormat().setGroupingUsed(false);
		serverPortSpinner.setEditor(editor);
		serverPortSpinner.setValue(new Integer(port));

		/* when we run integration test server mode, we use the passwords from integration test super admin */
		if (ConfigurationSetup.isIntegrationTestServerMenuEnabled()) {
			useridField.setText(TestAPI.SUPER_ADMIN.getUserId());
			passwordField.setText(TestAPI.SUPER_ADMIN.getApiToken());
		}

		serverField.setPreferredSize(new Dimension(300,30));
		useridField.setPreferredSize(new Dimension(200,30));
		passwordField.setPreferredSize(new Dimension(200,30));
		serverPortSpinner.setPreferredSize(new Dimension(100,30));

		panel.add(new JLabel("Server:"));
		panel.add(serverField);
		panel.add(new JLabel("Port:"));
		panel.add(serverPortSpinner);
		panel.add(new JLabel("User:"));
		panel.add(useridField);
		panel.add(new JLabel("API-Token:"));
		panel.add(passwordField);

		/* currently there is a bug - changes are not handled . So we disable edit fields etc.*/
		serverField.setEnabled(false);
		useridField.setEnabled(false);
		passwordField.setEnabled(false);
		serverPortSpinner.setEnabled(false);

		serverField.setToolTipText(ConfigurationSetup.ADMIN_SERVER.getId());
		useridField.setToolTipText(ConfigurationSetup.ADMIN_USERNAME.getId());
		passwordField.setToolTipText(ConfigurationSetup.ADMIN_APITOKEN.getId());
		serverPortSpinner.setToolTipText(ConfigurationSetup.ADMIN_SERVER_PORT.getId());
	}

	private void assertNotEmpty(String part, String missing) {
		if (part==null || part.isEmpty()) {
			throw new IllegalStateException("Missing configuration entry:"+missing+".\nYou have to configure these values:"+ConfigurationSetup.description());
		}

	}

	public int getPortNumber() {
		return ((Integer)serverPortSpinner.getValue()).intValue();
	}
}
