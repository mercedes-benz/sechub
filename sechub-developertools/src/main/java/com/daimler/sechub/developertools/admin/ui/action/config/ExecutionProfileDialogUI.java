// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.test.executionprofile.TestExecutionProfile;

public class ExecutionProfileDialogUI {

    private UIContext context;
    private JPanel mainPanel;
    private TestExecutionProfile config;
    private JTextField idTextField;
    private JTextArea descriptionTextArea;
    private JCheckBox enabledCheckBox;
    private JButton buttonOk;
    
    private boolean okPressed=false;
    private JPanel buttonPanel;
    private String title;
    private boolean idEditAllowed;

    public ExecutionProfileDialogUI(UIContext context, String title) {
        this(context, title, true, createExampleProfile());
    }

    public ExecutionProfileDialogUI(UIContext context, String title, boolean idEditAllowed, TestExecutionProfile profile) {
        this.context = context;
        this.config = profile;
        this.title=title;
        this.idEditAllowed=idEditAllowed;
    }

    UIContext getContext() {
        return context;
    }

    public boolean isOkPressed() {
        return okPressed;
    }
    
    public static TestExecutionProfile createExampleProfile() {
        TestExecutionProfile profile = new TestExecutionProfile();
        profile.description = "";
        return profile;
    }

    public void showDialog() {
        JDialog dialog = new JDialog(context.getFrame());
        dialog.setLayout(new BorderLayout());

        createMainPanel();
        createButtonPanel(dialog);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setTitle(title);
        dialog.setModal(true);
        dialog.setSize(new Dimension(600, 400));
        dialog.setLocationRelativeTo(context.getFrame());
        dialog.setVisible(true);
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    }

    private void createButtonPanel(JDialog dialog) {
        buttonPanel = new JPanel(new GridBagLayout());

        buttonOk = new JButton("Ok");
        buttonOk.addActionListener((event)-> {
            okPressed=true;
            dialog.setVisible(false);
            dialog.dispose();
        });
        
        buttonPanel.add(buttonOk,createLabelConstraint(0));
    }

    private void createMainPanel() {
        mainPanel = new JPanel(new GridBagLayout());

        int row = 0;
        /* id */
        idTextField = new JTextField(config.id);
        mainPanel.add(new JLabel("Id"), createLabelConstraint(row));
        mainPanel.add(idTextField, createComponentConstraint(row++));
        if (idEditAllowed) {
            idTextField.setBorder( BorderFactory.createLineBorder(Color.ORANGE, 2));
            idTextField.setToolTipText("mandatory field");
        }else {
            idTextField.setEditable(false);
        }
        descriptionTextArea = new JTextArea(config.description);
        mainPanel.add(new JLabel("Description"), createLabelConstraint(row));
        mainPanel.add(descriptionTextArea, createComponentConstraint(row++));
        
        /* enabled */
        enabledCheckBox = new JCheckBox("", config.enabled);
        mainPanel.add(new JLabel("Enabled"), createLabelConstraint(row));
        mainPanel.add(enabledCheckBox, createComponentConstraint(row++));
    }

    public TestExecutionProfile getUpdatedProfile() {
        config.enabled=enabledCheckBox.isSelected();
        if (idEditAllowed) {
            config.id=idTextField.getName();
        }
        config.description=descriptionTextArea.getText();
        return config;
    }

    private GridBagConstraints createComponentConstraint(int row) {
        GridBagConstraints gc = createConstraint(row, 1);
        gc.ipady = 5;
        gc.gridwidth = 3;
        gc.weightx = 0.5;
        return gc;
    }

    private GridBagConstraints createLabelConstraint(int row) {
        GridBagConstraints gc = createConstraint(row, 0);
        gc.ipady = 15;
        gc.weightx = 0.0;
        return gc;

    }

    private GridBagConstraints createConstraint(int row, int column) {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = column;
        c.gridy = row;
        c.ipadx=15;
        return c;
    }
}
