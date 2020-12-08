// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.developertools.admin.ConfigProvider;
import com.daimler.sechub.developertools.admin.DeveloperAdministration;
import com.daimler.sechub.developertools.admin.ErrorHandler;

public class DeveloperAdministrationUI implements ConfigProvider, ErrorHandler, UIContext {

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
    private JFrame frame;
    private boolean errors;
    private PDSConfigurationUI pdsConfigurationUI;

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

    @Override
    public PDSConfigurationUI getPDSConfigurationUI() {
        return pdsConfigurationUI;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return this;
    }

    private void start(String[] args) {
        administration = new DeveloperAdministration(this, this,this);

        useNimbusLookAndFeel();
        String env = ConfigurationSetup.SECHUB_ADMIN_ENVIRONMENT.getStringValueOrFail();

        frame = new JFrame(env + " - SecHub");
        ImageIcon imageIcon = new ImageIcon(DeveloperAdministrationUI.class.getClassLoader().getResource("sechub-logo.png"));
        Image image = imageIcon.getImage();
        frame.setIconImage(image);

        Container contentPane = frame.getContentPane();

        credentialUI = new CredentialUI(this);
        outputPanelUI = new OutputUI();
        commandPanelUI = new CommandUI(this);
        glassPaneUI = new GlassPaneUI(this, frame);
        dialogUI = new DialogUI(frame);
        pdsConfigurationUI = new PDSConfigurationUI(this);

        contentPane.add(outputPanelUI.getPanel(), BorderLayout.CENTER);
        JPanel northPanel = new JPanel(new BorderLayout());

        contentPane.add(northPanel, BorderLayout.NORTH);
        contentPane.add(commandPanelUI.getPanel(), BorderLayout.SOUTH);

        northPanel.add(credentialUI.getPanel(), BorderLayout.NORTH);
        northPanel.add(commandPanelUI.getToolBar(), BorderLayout.SOUTH);

        frame.setJMenuBar(commandPanelUI.getMenuBar());

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null); // centered...
        frame.setSize(1024, 768);
        frame.setVisible(true);
    }

    private void useNimbusLookAndFeel() {
        if (!ConfigurationSetup.isNimbusLookAndFeelEnabled()) {
            return;
        }
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
    public void resetErrors() {
        errors = false;
    }

    @Override
    public void handleError(String error) {
        errors = true;
        outputPanelUI.error(error);
    }

    @Override
    public boolean hasErrors() {
        return errors;
    }

    @Override
    public String getProtocol() {
        return credentialUI.protocolField.getText();
    }

    @Override
    public JFrame getFrame() {
        return frame;
    }

}
