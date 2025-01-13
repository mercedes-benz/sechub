// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class ComboxSelectionDialogUI<T> {

    private JLabel label;
    private JComboBox<T> combobox;

    private JButton cancelButton;
    private JButton okButton;

    private boolean okPressed;

    private JDialog dialog;

    public ComboxSelectionDialogUI(JFrame parentFrame, String title, String labelText, List<T> comboboxValues, T initialValue) {
        dialog = new JDialog(parentFrame, title, true);

        JPanel content = new JPanel(new BorderLayout());
        JPanel comboboxPanel = createComboBoxPanel(labelText, comboboxValues, initialValue);
        JPanel buttonPanel = createButtonPanel();

        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.add(comboboxPanel, BorderLayout.NORTH);
        content.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.getRootPane().setDefaultButton(okButton);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);

    }

    private JPanel createComboBoxPanel(String labelText, List<T> values, T initialValue) {
        JPanel comboboxPanel = new JPanel(new BorderLayout());
        label = new JLabel(labelText);

        DefaultComboBoxModel<T> model = new DefaultComboBoxModel<>();
        for (T value : values) {
            model.addElement(value);
        }

        combobox = new JComboBox<>(model);

        if (values.contains(initialValue)) {
            model.setSelectedItem(initialValue);
        }

        comboboxPanel.add(label, BorderLayout.NORTH);
        comboboxPanel.add(combobox, BorderLayout.SOUTH);
        return comboboxPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this::cancelPressed);

        okButton = new JButton("Ok");
        okButton.addActionListener(this::okPressed);

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    public void showDialog() {
        dialog.setVisible(true);
        combobox.requestFocusInWindow();
    }

    public T getSelectionFromCombobox() {
        @SuppressWarnings("unchecked")
        T inputValue = (T) combobox.getSelectedItem();
        return inputValue;
    }

    private Object okPressed(ActionEvent x) {
        okPressed = true;
        hideDialog();
        return null;
    }

    private void hideDialog() {
        dialog.setVisible(false);
    }

    private Object cancelPressed(ActionEvent x) {
        hideDialog();
        return null;
    }

    public boolean isOkPressed() {
        return okPressed;
    }

}