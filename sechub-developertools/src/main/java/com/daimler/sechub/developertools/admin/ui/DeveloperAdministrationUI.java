// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.developertools.admin.ConfigProvider;
import com.daimler.sechub.developertools.admin.DeveloperAdministration;

public class DeveloperAdministrationUI implements ConfigProvider, UIContext {

	private static final Logger LOG = LoggerFactory.getLogger(DeveloperAdministrationUI.class);

	public static void main(String[] args) {
		new DeveloperAdministrationUI().start(args);
	}

	private DeveloperAdministration administration;
	private CredentialUI credentialUI;
	private CommandUI commandPanelUI;
	private OutputUI outputPanelUI;
	private GlassPaneUI glassPaneUI;
	private DialogUI dialogUI;

	public DeveloperAdministration getAdministration() {
		return administration;
	}

	public OutputUI getOutputUI() {
		return outputPanelUI;
	}

	public CommandUI getCommandUI() {
		return commandPanelUI;
	}

	public CredentialUI getCredentialUI() {
		return credentialUI;
	}

	public GlassPaneUI getGlassPaneUI() {
		return glassPaneUI;
	}

	@Override
	public DialogUI getDialogUI() {
		return dialogUI;
	}

	private void start(String[] args) {

		useNimbusLookAndFeel();
		String env = ConfigurationSetup.SECHUB_ADMIN_ENVIRONMENT.getStringValueOrFail();

		JFrame frame = new JFrame(env+" - SecHub");
		ImageIcon imageIcon = new ImageIcon(DeveloperAdministrationUI.class.getClassLoader().getResource("sechub-logo.png"));
		Image image = imageIcon.getImage();
		frame.setIconImage(image);

		Container contentPane = frame.getContentPane();

		credentialUI = new CredentialUI();
		administration = new DeveloperAdministration(this);
		commandPanelUI = new CommandUI(this);
		outputPanelUI = new OutputUI();
		glassPaneUI = new GlassPaneUI(this, frame);
		dialogUI = new DialogUI(frame);

		contentPane.add(outputPanelUI.getPanel(), BorderLayout.CENTER);
		contentPane.add(credentialUI.getPanel(), BorderLayout.NORTH);
		contentPane.add(commandPanelUI.getPanel(), BorderLayout.SOUTH);

		frame.setJMenuBar(commandPanelUI.getMenuBar());

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(1024, 768);
		frame.setVisible(true);
	}

	private void useNimbusLookAndFeel() {
		try {
			UIManager.setLookAndFeel(new NimbusLookAndFeel());
		} catch (Exception e) {
			LOG.error("NimbusLookAndFeel init failed", e);
		}
	}

	@Override
	public String getApiToken() {
		return new String(credentialUI.passwordField.getPassword());
	}

	@Override
	public String getUser() {
		return credentialUI.useridField.getText();
	}

	@Override
	public String getServer() {
		return credentialUI.serverField.getText();
	}

	@Override
	public int getPort() {
		return credentialUI.getPortNumber();
	}

	@Override
	public void handleClientError(String error) {
		outputPanelUI.output("ERROR:\n" + error);
	}

	@Override
	public String getProtocol() {
		return credentialUI.protocolField.getText();
	}

}
