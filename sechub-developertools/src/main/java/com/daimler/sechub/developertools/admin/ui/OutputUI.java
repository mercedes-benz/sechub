// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.developertools.admin.ui.action.ActionSupport;

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
        JPopupMenu popup = new JPopupMenu();
        outputTextArea.setComponentPopupMenu(popup);

        ActionSupport support = new ActionSupport();
        support.apply(popup, support.createDefaultCutCopyAndPastActions());
        popup.addSeparator();
        popup.add(createCleanAction());

        panel.add(new JScrollPane(outputTextArea), BorderLayout.CENTER);

    }
    
    public CleanOutputAreaAction createCleanAction() {
        return new CleanOutputAreaAction();
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
    
    private class CleanOutputAreaAction extends AbstractAction{

        private static final long serialVersionUID = 1L;
        
        private CleanOutputAreaAction() {
            putValue(Action.NAME,"Clean");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            outputTextArea.setText("");
        }
        
    }

}
