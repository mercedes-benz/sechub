// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.developertools.admin.ui.action.ActionSupport;

public class OutputUI {

    private static final String OUTPUT_FONT_SETTINGS = ConfigurationSetup.getOutputFontSettings("courier 10");
    private JPanel panel;
    private JTextArea outputTextArea;

    private static final Logger LOG = LoggerFactory.getLogger(OutputUI.class);

    public JPanel getPanel() {
        return panel;
    }

    public OutputUI() {
        panel = new JPanel(new BorderLayout());

        outputTextArea = new JTextArea();
        outputTextArea.setFont(Font.decode(OUTPUT_FONT_SETTINGS));

        ActionSupport.getInstance().installAllTextActionsAsPopupTo(outputTextArea);
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

    public void error(String message, Throwable t) {
        LOG.error(message, t);
        output("ERROR:");
        output(message);
        if (t != null) {
            output(t.toString());
            output(">> Look into your shell or IDE console output for details!");
        }
    }

}
