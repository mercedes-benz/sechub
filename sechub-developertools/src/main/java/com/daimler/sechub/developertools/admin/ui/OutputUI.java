// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.developertools.admin.ui.action.ActionSupport;

public class OutputUI {
    private JPanel panel;
    private JTextArea outputTextArea;

    private static final Logger LOG = LoggerFactory.getLogger(OutputUI.class);

    public JPanel getPanel() {
        return panel;
    }

    public OutputUI() {
        panel = new JPanel(new BorderLayout());
//		panel.setBorder(BorderFactory.createLineBorder(Color.YELLOW));

        outputTextArea = new JTextArea();
        JPopupMenu popup = new JPopupMenu();
        outputTextArea.setComponentPopupMenu(popup);

        ActionSupport support = new ActionSupport();
        support.apply(popup, support.createDefaultCutCopyAndPastActions());

        panel.add(new JScrollPane(outputTextArea), BorderLayout.CENTER);

    }

    public void output(String text) {
        outputTextArea.append(text);
        outputTextArea.append("\n");
        outputTextArea.setCaretPosition(outputTextArea.getText().length());
    }

    public void error(String message) {
        error(message, null);
    }

    public void error(String message, Exception e) {
        LOG.error(message, e);
        output("ERROR:");
        output(message);
        if (e != null) {
            output(e.toString());
            output(">> Look into your shell or IDE console output for details!");
        }
    }

}
