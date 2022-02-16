// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.daimler.sechub.developertools.admin.ui.cache.InputCache;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class PDSConfigurationUI {

    private UIContext context;
    private JPanel panel;
    private JTextField userIdText;
    private JTextField hostnameText;
    private JTextField portText;
    private JPasswordField apiToken;

    private JPanel mainInputPanel = new JPanel();
    private JDialog dialog;

    public PDSConfigurationUI(UIContext context) {
        this.context = context;

        panel = new JPanel(new BorderLayout());

        hostnameText = addInput("hostname");
        portText = addInput("port");
        userIdText = addInput("userId");
        apiToken = addPassword("apiToken");

        getDataFromCache();

        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("OK");
        okButton.addActionListener((event) -> {
            setDataIntoCache();
            if (context != null) {
                context.getOutputUI().output("PDS data changed");
            }
            if (dialog != null) {
                dialog.dispose();
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener((event) -> {
            getDataFromCache();
            if (context != null) {
                context.getOutputUI().output("PDS data reverted to last cache values");
            }
            if (dialog != null) {
                dialog.dispose();
            }
        });
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(mainInputPanel);

    }

    private void getDataFromCache() {
        hostnameText.setText(InputCache.DEFAULT.get(InputCacheIdentifier.PDS_HOSTNAME));
        portText.setText(InputCache.DEFAULT.get(InputCacheIdentifier.PDS_PORT));
        userIdText.setText(InputCache.DEFAULT.get(InputCacheIdentifier.PDS_USER));
        apiToken.setText(InputCache.DEFAULT.get(InputCacheIdentifier.PDS_APITOKEN));
    }

    private void setDataIntoCache() {
        InputCache.DEFAULT.set(InputCacheIdentifier.PDS_HOSTNAME, hostnameText.getText());
        InputCache.DEFAULT.set(InputCacheIdentifier.PDS_PORT, portText.getText());
        InputCache.DEFAULT.set(InputCacheIdentifier.PDS_USER, userIdText.getText());
        InputCache.DEFAULT.set(InputCacheIdentifier.PDS_APITOKEN, new String(apiToken.getPassword()));
    }

    public String getUserId() {
        return userIdText.getText();
    }

    public String getHostName() {
        return hostnameText.getText();
    }

    public int getPort() {
        try {
            return Integer.parseInt(portText.getText());
        } catch (NumberFormatException e) {
            context.getErrorHandler().handleError("port not a number!!! Fallback to 8444");
            return 8444;
        }
    }

    public String getApiToken() {
        return new String(apiToken.getPassword());
    }

    private JTextField addInput(String string) {
        return addInput(string, false);
    }

    private JPasswordField addPassword(String string) {
        return (JPasswordField) addInput(string, true);
    }

    private JTextField addInput(String string, boolean password) {
        JPanel inputPanel = new JPanel();
        JLabel label = new JLabel(string);
        label.setPreferredSize(new Dimension(200, 30));
        inputPanel.add(label);
        JTextField textfield;
        if (password) {
            textfield = new JPasswordField();
        } else {
            textfield = new JTextField();
        }
        textfield.setPreferredSize(new Dimension(200, 30));
        inputPanel.add(textfield);
        mainInputPanel.add(inputPanel);
        return textfield;
    }

    public JPanel getPanel() {
        return panel;
    }

    public void showInside(JFrame frame) {
        JPanel panel = getPanel();

        dialog = new JDialog(frame, "PDS configuration", true);
        dialog.getContentPane().add(panel);
        dialog.setLocationRelativeTo(frame);
        dialog.setSize(new Dimension(600, 300));
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Testme");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
        new PDSConfigurationUI(null).showInside(frame);
    }
}
